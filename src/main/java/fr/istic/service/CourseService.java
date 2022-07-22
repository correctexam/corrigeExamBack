package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.config.Constants;
import fr.istic.domain.Course;
import fr.istic.domain.User;
import fr.istic.service.customdto.ListUserModelShare;
import fr.istic.service.customdto.UserModelShare;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.mapper.CourseMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class CourseService {

    private final Logger log = LoggerFactory.getLogger(CourseService.class);

    @Inject
    CourseMapper courseMapper;

    @Transactional
    public CourseDTO persistOrUpdate(CourseDTO courseDTO) {
        log.debug("Request to save Course : {}", courseDTO);
        var course = courseMapper.toEntity(courseDTO);
        course = Course.persistOrUpdate(course);
        return courseMapper.toDto(course);
    }

    /**
     * Delete the Course by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Course : {}", id);
        Course.findByIdOptional(id).ifPresent(course -> {
            course.delete();
        });
    }

    /**
     * Get one course by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<CourseDTO> findOne(Long id) {
        log.debug("Request to get Course : {}", id);
        return Course.findOneWithEagerRelationships(id)
            .map(course -> courseMapper.toDto((Course) course));
    }

    /**
     * Get all the courses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseDTO> findAll(Page page) {
        log.debug("Request to get all Courses");
        return new Paged<>(Course.findAll().page(page))
            .map(course -> courseMapper.toDto((Course) course));
    }

    /**
     * Get all the courses with eager load of many-to-many relationships.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseDTO> findAllWithEagerRelationships(Page page) {
        var courses = Course.findAllWithEagerRelationships().page(page).list();
        var totalCount = Course.findAll().count();
        var pageCount = Course.findAll().page(page).pageCount();
        return new Paged<>(page.index, page.size, totalCount, pageCount, courses)
            .map(course -> courseMapper.toDto((Course) course));
    }

        /**
     * Get all the courses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseDTO> findAll4User(Page page, User u) {
        log.debug("Request to get all Courses");
        return new Paged<>(Course.findByProfIsCurrentUser(u.login).page(page))
            .map(course -> courseMapper.toDto((Course) course));
    }


        /**
     * Get all the courseGroups.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public ListUserModelShare getAllListUserModelShare(long courseid, String currentlogin) {
        log.debug("Request to get all getAllListUserModelShare");
        List<User> users  =Course.findByProfIsCurrentUserFetchProf(courseid).list();
        List<String> logins =  users.stream().map(u -> u.login).collect(Collectors.toList());
        logins.add("admin");
        logins.add("user");
        logins.add("system");
        List<User> users1  =User.findAllByLoginNot(Constants.ANONYMOUS_USER);
        users1 = users1.stream().filter(u -> !logins.contains(u.login)).collect(Collectors.toList());
        users = users.stream().filter(u ->  !u.login.equals(currentlogin)).collect(Collectors.toList());
        ListUserModelShare res = new ListUserModelShare();
        res.setShared(users.stream().map(u-> new UserModelShare(u.lastName, u.firstName, u.login)).collect(Collectors.toList()));
        res.setAvailables(users1.stream().map(u-> new UserModelShare(u.lastName, u.firstName, u.login)).collect(Collectors.toList()));
        return res;
    }


    @Transactional
    public void addProfs(long courseId, List<UserModelShare> profs){
        Course c = Course.findById(courseId);
        List<User> profsToAdd = new ArrayList<>();
        profs.forEach(p-> {
            profsToAdd.add(User.findOneByLogin(p.getLogin()).get());
        });
        c.profs.addAll(profsToAdd);
        Course.persistOrUpdate(c);

    }
    @Transactional
    public void removeProfs(long courseId, List<UserModelShare> profs){
        Course c = Course.findById(courseId);
        List<User> profsToAdd = new ArrayList<>();
        profs.forEach(p-> {
            profsToAdd.add(User.findOneByLogin(p.getLogin()).get());
        });
        c.profs.removeAll(profsToAdd);
        Course.persistOrUpdate(c);

    }




}
