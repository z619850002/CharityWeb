package com.tongji.charityweb.service;

import com.tongji.charityweb.model.comment.ProjectComment;
import com.tongji.charityweb.model.project.*;
import com.tongji.charityweb.model.repository.Repository;
import com.tongji.charityweb.model.repository.RepositoryID;
import com.tongji.charityweb.model.user.User;
import com.tongji.charityweb.repository.project.ParRepository;
import com.tongji.charityweb.repository.project.ProFolRepository;
import com.tongji.charityweb.repository.project.ProjectRepository;
import com.tongji.charityweb.repository.repository.RepRepository;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProFolRepository proFolRepository;
    @Autowired
    private ParRepository parRepository;
    @Autowired
    private RepRepository repRepository;





    //类似创建有关联的表时，例如想对某位user 添加repository时，需要在user对象中添加
//因为user 和 repository之间存在关系
    public boolean createProject( String repName, String projName, String userName,String context)
    {
        try
        {
            RepositoryID repositoryID = new RepositoryID(userName, repName);
            Repository repository = repRepository.getOne(repositoryID);


            Project newProject = new Project(repName,projName,userName);
            newProject.setContext(context);

            repository.addProject(newProject);
            repRepository.save(repository);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public Project getMostHotProject(){
        Project project  = projectRepository.findTopByOrderByFollowerNumDesc();
        return project;
    }

    public boolean deleteProject(String projName, String repName,String userName) {
        try{
            ProjectID projectID = new ProjectID(projName, repName, userName);
            RepositoryID repositoryID = new RepositoryID(userName, repName);
            Repository repository = repRepository.findOne(repositoryID);
            Project project = projectRepository.findOne(projectID);

            if(repository == null | project == null)
                return false;


            repository.deleteProject(project);
            repRepository.save(repository);
            return true;
        }
        catch (Exception e){
            return false;
        }

    }

    public List<Project> getParticipateProjects(String parName)
    {
        List<Project> projects = new ArrayList<>();
        try {
            List<Participate> participates = parRepository.findByParName(parName);

            for (Participate x : participates) {
                String repName = x.getRepName();
                String projName = x.getProjName();
                String userName = x.getUserName();
                projects.add(projectRepository.findOne(new ProjectID(projName, repName, userName)));
            }
        }
        catch (Exception e){
            return null;
        }
        return projects;
    }


    public Project findOneProject(String projName, String repName, String userName) {
        try {
            return projectRepository.findOne(new ProjectID(projName, repName, userName));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Project> getAllProjectsOrderByParNum() {
        try {
            return projectRepository.findAll(new Sort(Sort.Direction.DESC, "participateNum"));
        } catch (Exception e) {
            return null;
        }
    }

    public List<Project> getAllProjectsOrderByFolNum() {
        try {
            return projectRepository.findAll(new Sort(Sort.Direction.DESC, "followerNum"));
        } catch (Exception e) {
            return null;
        }
    }

    public Page<Project> getProjectPageOrderByFolNum(int page, int size) {
        try {
            Pageable pageable = new PageRequest(page,size, Sort.Direction.DESC,"followerNum");
            return projectRepository.findAll(pageable);
        } catch (Exception e) {
            return null;
        }
    }

    public Page<Project> getProjectPageOrderByParNum(int page, int size) {
        try {
            Pageable pageable = new PageRequest(page,size, Sort.Direction.DESC,"participateNum");
            return projectRepository.findAll(pageable);
        } catch (Exception e) {
            return null;
        }
    }
    //public Project getTopByFollowerNum()
    //{
    //    return projectRepository.findTopByFollowerNum();
    //}
    public Page<Project> getUserParticipateProject(int page,int size,String username)
    {
        try
        {
            Pageable pageable = new PageRequest(page,size);
            Page<Participate> participates = parRepository.findAllByParName(username, pageable);
            Page<Project> projects = participates.map(new Converter<Participate, Project>()
            {
                @Override
                public Project convert(Participate participate)
                {
                    Project project = findOneProject(participate.getProjName(), participate.getRepName(), participate.getUserName());
                    return project;
                }
            });

            return projects;
        }
        catch (Exception e) {
            return null;
        }
    }

    public boolean createProFollower(String projName, String repName,  String followerName, String userName) {
        try {
            ProjectID projectID = new ProjectID(projName,repName,  userName);
            ProjectFollower projectFollower = new ProjectFollower(userName, repName,projName, followerName);
            Project project = projectRepository.findOne(projectID);
            project.addFollower(projectFollower);
            project.setFollowerNum(project.getFollowerNum()+1);
            projectFollower.setProject(project);
            projectRepository.save(project);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean deleteProFollower(String projName, String repName,  String followerName, String userName) {
        try {
            ProjectID projectID = new ProjectID(projName,repName,  userName);
            Project project = projectRepository.findOne(projectID);
            project.setFollowerNum(project.getFollowerNum()-1);
            projectRepository.save(project);
            ProjectFollower projectFollower = proFolRepository.findOne(new ProjectFollowerID(userName, repName, projName, followerName));
            proFolRepository.delete(projectFollower);
            return true;
        } catch  (Exception e) {
            return false;
        }
    }

    public ProjectFollower findOneFollower(String projName, String repName, String userName, String followerName) {
        try {
            return proFolRepository.findOne(new ProjectFollowerID(userName,repName,projName,followerName));
        } catch (Exception e) {
            e.printStackTrace();;
            return null;
        }
    }

    public Participate findOneParticipater(String projName, String repName, String userName, String parName) {
        try {
            return parRepository.findOne(new ProjectParticipateID(userName,repName,projName,parName));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean participateProject(String userName, String repName, String projName, String parName)
    {
        try
        {
            Participate newParticipate = new Participate(userName, repName, projName, parName);
            Project project = projectRepository.getOne(new ProjectID(projName, repName, userName));

            project.addParticipate(newParticipate);
            projectRepository.save(project);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }


    public boolean deleteParticipate(String userName, String repName, String projName, String parName)
    {
        try{
            parRepository.delete(new ProjectParticipateID(userName, repName, projName, parName));
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

    public String showAllProFollower(String userName, String repName, String projName) {
        try {
            List<ProjectFollower> followerList;
            Project project = projectRepository.findOne(new ProjectID(projName,repName,userName));
            followerList = project.getFollowers();

            String followers = "";
            for(ProjectFollower x : followerList) {
                followers += x.getFollowerName()+"\n";
            }
            return followers;
        } catch (Exception e) {
            return "showAllProFollower error";
        }
    }

    public List<Project> findProjNameLike(String toSearch)
    {
        return projectRepository.findByProjNameLike(toSearch);
    }
}
