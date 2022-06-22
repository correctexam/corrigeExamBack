package fr.istic.service;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.SecurityContext;

import fr.istic.domain.Authority;
import fr.istic.domain.Comments;
import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.domain.Zone;

@ApplicationScoped
public class SecurityService {
    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }


    public  boolean canAccess(SecurityContext ctx,long id, Class entity ){
    if (ctx.getUserPrincipal() == null ) return false;
        var userLogin = Optional
        .ofNullable(ctx.getUserPrincipal().getName());
    if (!userLogin.isPresent()){
        throw new AccountResourceException("Current user login not found");
    }
    var user = User.findOneByLogin(userLogin.get());
    if (!user.isPresent()) {
        throw new AccountResourceException("User could not be found");
    }
        if (user.get().authorities.size() == 1 && user.get().authorities.stream().anyMatch(e1-> e1.equals(new Authority("ROLE_USER")))){
            long number = 0;
            if (entity.equals(Student.class)){
                number = Student.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Course.class)){
                number = Course.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Comments.class)){
                number = Comments.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(CourseGroup.class)){
                number = CourseGroup.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Exam.class)){
                number = Exam.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(ExamSheet.class)){
                number = ExamSheet.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(FinalResult.class)){
                number = FinalResult.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(GradedComment.class)){
                number = GradedComment.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Question.class)){
                number = Question.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Scan.class)){
                number = Scan.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(StudentResponse.class)){
                number = StudentResponse.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Template.class)){
                number = Template.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(TextComment.class)){
                number = TextComment.canAccess(id, userLogin.get()).count();
            } else if (entity.equals(Zone.class)){
                number = Zone.canAccess(id, userLogin.get()).count();
            }
            return number > 0 ;
        }
        else if (user.get().authorities.size() >= 1 && user.get().authorities.stream().anyMatch(e1-> e1.equals(new Authority("ROLE_ADMIN")))){

            return true;

        } else {

            return false;
        }
    }





}
