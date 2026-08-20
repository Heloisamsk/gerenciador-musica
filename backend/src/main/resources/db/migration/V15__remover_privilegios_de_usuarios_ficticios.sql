UPDATE usuario
SET role = 'USER'
WHERE id BETWEEN 1 AND 60
  AND role = 'ADMIN';