package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Course;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface LearningDashboardRepository extends Repository<Course, UUID> {
    String PROGRESS = """
        with counts as (
          select u.id as studentId, c.id as courseId, e.enrolledAt as enrolledAt, u.name as name,
            (select count(l) from Lesson l where l.course.id=c.id) as totalLessons,
            (select count(q) from Question q where q.course.id=c.id) as totalQuestions,
            (select count(p) from User s join s.lessonProgress p join Lesson l on l.id=key(p).id
              where s.id=u.id and l.course.id=c.id and p.completed=true) as completedLessons,
            (select count(q) from Question q where q.course.id=c.id
              and q.id in (select a from User s join s.passedQuestionIds a where s.id=u.id)) as passedQuestions
          from User u join u.enrollments e join Course c on c.id=key(e).id
        ), progress as (
          select studentId as studentId, courseId as courseId, enrolledAt as enrolledAt, name as name,
            totalLessons as totalLessons, totalQuestions as totalQuestions,
            completedLessons as completedLessons, passedQuestions as passedQuestions,
            coalesce(round(100.0*(completedLessons+passedQuestions)/nullif(totalLessons+totalQuestions,0),1),0) as percentage,
            (totalLessons+totalQuestions>0 and completedLessons=totalLessons and passedQuestions=totalQuestions) as completed
          from counts
        )
        """;

    @Query(PROGRESS + """
        select new map(c.id as id, c.name as name, c.category as category, p.enrolledAt as enrolled_at,
          p.totalLessons as total_lessons, p.completedLessons as completed_lessons,
          p.totalQuestions as total_questions, p.passedQuestions as passed_questions,
          p.percentage as progress_percentage, p.completed as completed)
        from progress p join Course c on c.id=p.courseId
        where p.studentId=:studentId order by p.enrolledAt desc,c.id
        """)
    List<Map<String,Object>> findStudentCourses(UUID studentId);

    long countByProfessorId(UUID professorId);

    @Query(PROGRESS + """
        select new map(c.id as id, c.name as name, count(p.studentId) as enrolled_students,
          sum(case when p.completed=true then 1L else 0L end) as completed_students,
          coalesce(round(avg(p.percentage),1),0) as average_progress,
          coalesce(round(100.0*sum(case when p.completed=true then 1L else 0L end)/nullif(count(p.studentId),0),1),0) as completion_rate)
        from Course c left join progress p on p.courseId=c.id
        where c.professor.id=:professorId group by c.id,c.name order by c.name,c.id
        """)
    List<Map<String,Object>> findTeachingCourses(UUID professorId, Pageable pageable);

    @Query(PROGRESS + """
        select new map(p.studentId as id, p.name as name, p.enrolledAt as enrolled_at,
          p.totalLessons as total_lessons, p.completedLessons as completed_lessons,
          p.totalQuestions as total_questions, p.passedQuestions as passed_questions,
          p.percentage as progress_percentage, p.completed as completed,
          (select count(a) from QuestionAttempt a where a.studentId=p.studentId and a.courseId=p.courseId) as attempts,
          (select round(100.0*sum(case when a.correct=true then 1L else 0L end)/nullif(count(a),0),1)
            from QuestionAttempt a where a.studentId=p.studentId and a.courseId=p.courseId) as accuracy)
        from progress p where p.courseId=:courseId order by p.enrolledAt,p.studentId
        """)
    List<Map<String,Object>> findStudents(UUID courseId);

    @Query("""
        select new map(q.id as id, q.prompt as prompt, count(a.id) as attempts,
          coalesce(round(100.0*sum(case when a.correct=true then 1L else 0L end)/nullif(count(a.id),0),1),0) as accuracy)
        from Question q left join QuestionAttempt a on a.questionId=q.id
        where q.course.id=:courseId
          and (a.id is null or exists(select u.id from User u where u.id=a.studentId))
        group by q.id,q.prompt,q.afterLesson,q.position order by q.afterLesson,q.position,q.id
        """)
    List<Map<String,Object>> findQuestionStats(UUID courseId);
}
