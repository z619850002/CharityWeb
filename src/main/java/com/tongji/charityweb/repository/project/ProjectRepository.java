package com.tongji.charityweb.repository.project;

import com.tongji.charityweb.model.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by LSL on 2017/11/21
 *
 */

@Transactional
@Repository
public interface ProjectRepository extends JpaRepository<Project,Long>
{

}