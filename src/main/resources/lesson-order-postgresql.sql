WITH legacy_courses AS (
    SELECT DISTINCT course_id FROM tb_lesson WHERE deleted_at IS NULL AND position <= 0
), ranked AS (
    SELECT l.id, l.course_id, l.position AS old_position,
           row_number() OVER (PARTITION BY l.course_id ORDER BY l.position, l.title, l.id)::integer AS new_position
    FROM tb_lesson l JOIN legacy_courses c ON c.course_id = l.course_id
    WHERE l.deleted_at IS NULL
), question_moves AS (
    UPDATE tb_question q SET after_lesson = r.new_position
    FROM ranked r
    WHERE q.course_id = r.course_id AND q.after_lesson = r.old_position
      AND r.old_position > 0 AND q.deleted_at IS NULL
    RETURNING q.id
)
UPDATE tb_lesson l SET position = r.new_position FROM ranked r WHERE l.id = r.id;
