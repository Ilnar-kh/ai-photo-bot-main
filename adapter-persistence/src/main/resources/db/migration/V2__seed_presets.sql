INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Studio Portrait', 'fireworks-v1', '{"style":"studio","lighting":"soft"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Studio Portrait');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Moody Noir', 'fireworks-v1', '{"style":"noir","contrast":"high"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Moody Noir');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Warm Sunset', 'fireworks-v1', '{"palette":"warm","time":"sunset"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Warm Sunset');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Corporate Clean', 'fireworks-v1', '{"style":"corporate","background":"neutral"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Corporate Clean');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Fashion Editorial', 'fireworks-v1', '{"style":"editorial","mood":"bold"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Fashion Editorial');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Cinematic Blue', 'fireworks-v1', '{"palette":"cool","grade":"cinematic"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Cinematic Blue');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'Dreamy Pastel', 'fireworks-v1', '{"palette":"pastel","mood":"dreamy"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'Dreamy Pastel');

INSERT INTO presets (id, name, model, params_json)
SELECT gen_random_uuid(), 'High Contrast', 'fireworks-v1', '{"contrast":"extreme","lighting":"dramatic"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM presets WHERE name = 'High Contrast');
