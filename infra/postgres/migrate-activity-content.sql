-- Migration pour bases activity_db existantes (colonnes details / prerequisites).
\c activity_db

ALTER TABLE activities ADD COLUMN IF NOT EXISTS details varchar(5000);
ALTER TABLE activities ADD COLUMN IF NOT EXISTS prerequisites varchar(2000);

UPDATE activities
SET details = description
WHERE details IS NULL OR details = '';

UPDATE activities
SET prerequisites = E'• Âge recommandé : 6 à 12 ans\n• Tenue confortable et chaussures adaptées\n• Bouteille d''eau et goûter\n• Autorisation parentale signée le jour J'
WHERE prerequisites IS NULL OR prerequisites = '';
