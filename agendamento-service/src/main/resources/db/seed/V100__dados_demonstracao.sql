-- Dados apenas para o ambiente local do docker-compose.
-- Todos os usuarios usam a senha "senha123" (hash BCrypt de demonstracao).
INSERT INTO users (nome, email, senha, role)
SELECT 'Dra. Ana Demo', 'medico.demo@hospital.local',
       '$2a$10$0/NLXH1O6GhMEAkdjJp3S.VBG.uqAM.P4cnMEvLJo0ljPsF1xXD.G', 'MEDICO'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'medico.demo@hospital.local');

INSERT INTO users (nome, email, senha, role)
SELECT 'Enfermeiro Demo', 'enfermeiro.demo@hospital.local',
       '$2a$10$0/NLXH1O6GhMEAkdjJp3S.VBG.uqAM.P4cnMEvLJo0ljPsF1xXD.G', 'ENFERMEIRO'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'enfermeiro.demo@hospital.local');

INSERT INTO users (nome, email, senha, role)
SELECT 'Paciente Demo', 'paciente.demo@hospital.local',
       '$2a$10$0/NLXH1O6GhMEAkdjJp3S.VBG.uqAM.P4cnMEvLJo0ljPsF1xXD.G', 'PACIENTE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'paciente.demo@hospital.local');

INSERT INTO consultas (paciente_id, medico_id, data_hora, observacoes, status)
SELECT paciente.id, medico.id, CURRENT_TIMESTAMP + INTERVAL '7 days',
       'Consulta futura criada pelo seed', 'AGENDADA'
FROM users paciente
JOIN users medico ON medico.email = 'medico.demo@hospital.local'
WHERE paciente.email = 'paciente.demo@hospital.local'
  AND NOT EXISTS (
      SELECT 1 FROM consultas c
      WHERE c.paciente_id = paciente.id
        AND c.observacoes = 'Consulta futura criada pelo seed'
  );

INSERT INTO consultas (paciente_id, medico_id, data_hora, observacoes, status)
SELECT paciente.id, medico.id, CURRENT_TIMESTAMP - INTERVAL '30 days',
       'Consulta historica criada pelo seed', 'REALIZADA'
FROM users paciente
JOIN users medico ON medico.email = 'medico.demo@hospital.local'
WHERE paciente.email = 'paciente.demo@hospital.local'
  AND NOT EXISTS (
      SELECT 1 FROM consultas c
      WHERE c.paciente_id = paciente.id
        AND c.observacoes = 'Consulta historica criada pelo seed'
  );
