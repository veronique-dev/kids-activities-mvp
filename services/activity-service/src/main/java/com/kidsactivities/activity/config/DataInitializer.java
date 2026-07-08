package com.kidsactivities.activity.config;

import com.kidsactivities.activity.entity.Activity;
import com.kidsactivities.activity.entity.Catalog;
import com.kidsactivities.activity.repository.ActivityRepository;
import com.kidsactivities.activity.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ActivityRepository activityRepository;
    private final CatalogRepository catalogRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Catalog> catalogs = ensureCatalogs();
        migrateLegacyActivities(catalogs.get(0));

        if (activityRepository.count() == 0) {
            seedActivities(catalogs);
        }
    }

    private List<Catalog> ensureCatalogs() {
        if (catalogRepository.count() > 0) {
            return catalogRepository.findAll();
        }

        return List.of(
                saveCatalog("Arts & Créativité", "Peinture, dessin, bricolage et expression artistique.", "🎨", 1),
                saveCatalog("Spectacle & Théâtre", "Théâtre, improvisation, danse et arts de la scène.", "🎭", 2),
                saveCatalog("Sports & Nature", "Sport, natation, randonnée et activités de plein air.", "⚽", 3),
                saveCatalog("Vacances & Stages", "Stages intensifs pendant les vacances scolaires.", "🏕️", 4)
        );
    }

    private Catalog saveCatalog(String name, String description, String emoji, int sortOrder) {
        return catalogRepository.save(Catalog.builder()
                .name(name)
                .description(description)
                .emoji(emoji)
                .active(true)
                .sortOrder(sortOrder)
                .build());
    }

    private void migrateLegacyActivities(Catalog defaultCatalog) {
        Map<String, ActivityContent> contentByTitle = activityContentByTitle();

        for (Activity activity : activityRepository.findAll()) {
            boolean changed = false;

            if (activity.getCatalog() == null) {
                activity.setCatalog(defaultCatalog);
                changed = true;
            }

            if (activity.getRegistrationDeadline() == null && activity.getStartDateTime() != null) {
                activity.setRegistrationDeadline(activity.getStartDateTime().minusDays(2));
                changed = true;
            }

            ActivityContent content = contentByTitle.get(activity.getTitle());
            if (!StringUtils.hasText(activity.getDetails())) {
                activity.setDetails(content != null
                        ? content.details()
                        : defaultDetails(activity));
                changed = true;
            }

            if (!StringUtils.hasText(activity.getPrerequisites())) {
                activity.setPrerequisites(content != null
                        ? content.prerequisites()
                        : defaultPrerequisites(activity));
                changed = true;
            }

            if (changed) {
                activityRepository.save(activity);
            }
        }
    }

    private String defaultDetails(Activity activity) {
        return """
                %s

                Au programme : découverte, jeux et apprentissage encadré par un animateur diplômé.
                Les parents sont accueillis 15 minutes avant le début pour la prise en charge.
                """.formatted(activity.getDescription());
    }

    private String defaultPrerequisites(Activity activity) {
        return """
                • Âge recommandé : 6 à 12 ans
                • Tenue confortable et chaussures adaptées
                • Bouteille d'eau et goûter
                • Autorisation parentale signée le jour J
                """;
    }

    private void seedActivities(List<Catalog> catalogs) {
        Catalog arts = catalogs.get(0);
        Catalog theatre = catalogs.get(1);
        Catalog sports = catalogs.get(2);
        Catalog holidays = catalogs.get(3);

        LocalDateTime now = LocalDateTime.now();
        Map<String, ActivityContent> content = activityContentByTitle();

        saveSeed(arts, content.get("Atelier peinture"),
                "Atelier peinture",
                "Découverte de la peinture acrylique pour les 6-10 ans.",
                now.plusDays(7).withHour(14).withMinute(0),
                now.plusDays(5).withHour(23).withMinute(59),
                "Maison des associations, Paris 11e", 12, new BigDecimal("25.00"));

        saveSeed(theatre, content.get("Stage théâtre vacances"),
                "Stage théâtre vacances",
                "Une semaine de théâtre et improvisation pour les 8-14 ans.",
                now.plusDays(14).withHour(9).withMinute(0),
                now.plusDays(10).withHour(18).withMinute(0),
                "Théâtre municipal, Lyon", 20, new BigDecimal("120.00"));

        saveSeed(sports, content.get("Cours de natation débutant"),
                "Cours de natation débutant",
                "Apprentissage des bases de la natation en petit groupe.",
                now.plusDays(3).withHour(10).withMinute(30),
                now.plusDays(2).withHour(12).withMinute(0),
                "Piscine Olympique, Marseille", 8, new BigDecimal("18.50"));

        saveSeed(arts, content.get("Atelier origami"),
                "Atelier origami",
                "Plier le papier pour créer des animaux et des formes magiques.",
                now.plusDays(5).withHour(16).withMinute(0),
                now.plusDays(4).withHour(20).withMinute(0),
                "Bibliothèque jeunesse, Toulouse", 15, new BigDecimal("12.00"));

        saveSeed(sports, content.get("Initiation au basket"),
                "Initiation au basket",
                "Premiers pas avec le ballon, dribbles et mini-matchs.",
                now.plusDays(6).withHour(11).withMinute(0),
                now.plusDays(4).withHour(18).withMinute(0),
                "Gymnase Jean Moulin, Bordeaux", 16, new BigDecimal("15.00"));

        saveSeed(holidays, content.get("Stage cirque été"),
                "Stage cirque été",
                "Jonglage, équilibre et acrobatie pour les 7-12 ans.",
                now.plusDays(21).withHour(10).withMinute(0),
                now.plusDays(14).withHour(23).withMinute(59),
                "Centre de loisirs, Nantes", 18, new BigDecimal("95.00"));

        saveSeed(theatre, content.get("Spectacle de marionnettes"),
                "Spectacle de marionnettes",
                "Atelier création puis représentation devant les parents.",
                now.plusDays(9).withHour(15).withMinute(0),
                now.plusDays(7).withHour(12).withMinute(0),
                "Salle polyvalente, Lille", 10, new BigDecimal("20.00"));

        saveSeed(sports, content.get("Randonnée nature"),
                "Randonnée nature",
                "Balade guidée avec jeux de découverte de la faune et la flore.",
                now.plusDays(4).withHour(9).withMinute(30),
                now.plusDays(3).withHour(18).withMinute(0),
                "Parc naturel, Grenoble", 14, new BigDecimal("10.00"));
    }

    private void saveSeed(
            Catalog catalog,
            ActivityContent content,
            String title,
            String description,
            LocalDateTime startDateTime,
            LocalDateTime registrationDeadline,
            String location,
            int maxCapacity,
            BigDecimal price
    ) {
        activityRepository.save(Activity.builder()
                .title(title)
                .description(description)
                .details(content.details())
                .prerequisites(content.prerequisites())
                .startDateTime(startDateTime)
                .registrationDeadline(registrationDeadline)
                .location(location)
                .maxCapacity(maxCapacity)
                .availableSpots(maxCapacity)
                .price(price)
                .active(true)
                .catalog(catalog)
                .build());
    }

    private Map<String, ActivityContent> activityContentByTitle() {
        return Map.ofEntries(
                Map.entry("Atelier peinture", new ActivityContent(
                        """
                                Un après-midi créatif pour explorer les couleurs et laisser parler son imagination !
                                
                                Déroulement :
                                • 14h00 — Accueil et présentation du matériel
                                • 14h15 — Techniques de base (mélange des couleurs, pinceaux)
                                • 15h00 — Création libre sur toile (thème : « Mon animal préféré »)
                                • 16h00 — Goûter collectif
                                • 16h30 — Exposition des œuvres et retour aux parents
                                
                                Matériel fourni : tablier, toiles, peintures acryliques, pinceaux et chevalet.
                                Chaque enfant repart avec sa création.
                                """,
                        """
                                • Âge : 6 à 10 ans
                                • Aucune expérience en peinture requise
                                • Tenue qui peut être salie (tablier fourni)
                                • Prévenir l'animateur de toute allergie (peintures non toxiques)
                                """
                )),
                Map.entry("Stage théâtre vacances", new ActivityContent(
                        """
                                Cinq jours pour monter un spectacle de A à Z : improvisation, jeux d'acteur et répétitions.
                                
                                Programme journalier (9h-16h) :
                                • Matin — Échauffement corporel, voix et travail en groupe
                                • Midi — Pause déjeuner (picnic ou cantine selon inscription)
                                • Après-midi — Mise en scène, costumes et répétitions
                                • Vendredi 17h — Spectacle devant les familles (45 min)
                                
                                Encadrement : 2 comédiens professionnels + 1 aide pédagogique.
                                Thème de la semaine : « Voyage autour du monde ».
                                """,
                        """
                                • Âge : 8 à 14 ans
                                • Savoir lire un texte court (ou être accompagné)
                                • Apporter un pique-nique ou souscrire à la formule cantine (+35 €)
                                • Tenue souple et chaussures de sport
                                • Engagement sur la semaine complète
                                """
                )),
                Map.entry("Cours de natation débutant", new ActivityContent(
                        """
                                Séance de 1h30 en bassin peu profonde pour apprendre à l'eau en toute confiance.
                                
                                Contenu :
                                • Familiarisation avec l'eau (immersion, souffle)
                                • Flottaison ventrale et dorsale avec matériel adapté
                                • Premiers mouvements de bras et de jambes
                                • Jeux aquatiques en fin de séance
                                
                                Ratio : 1 moniteur pour 4 enfants maximum.
                                Vestiaires et douches sur place.
                                """,
                        """
                                • Âge : 5 à 8 ans
                                • Maillot de bain, serviette, tongs
                                • Bonnet de bain obligatoire (vente sur place : 5 €)
                                • Certificat médical de non-contre-indication à la natation (< 1 an)
                                • L'enfant ne doit pas avoir peur de l'eau (peur légère acceptée)
                                """
                )),
                Map.entry("Atelier origami", new ActivityContent(
                        """
                                Plier, plier, plier… et voilà des grues, des grenouilles et des étoiles !
                                
                                Au fil de l'atelier (2h) :
                                • Découverte du papier washi et des plis de base
                                • Réalisation guidée de 3 modèles progressifs
                                • Projet libre avec aide de l'animateur
                                • Chaque enfant repart avec un mini-livret de plis
                                
                                Ambiance calme et concentrée, idéale pour développer la patience et la précision.
                                """,
                        """
                                • Âge : 7 à 12 ans
                                • Bonne dextérité des doigts (capacité à suivre des consignes simples)
                                • Lunettes si portées habituellement
                                • Papier fourni — pas de matériel à apporter
                                """
                )),
                Map.entry("Initiation au basket", new ActivityContent(
                        """
                                Découvrir le basket en s'amusant : dribbles, passes et mini-matchs !
                                
                                Séance de 2h :
                                • 11h00 — Échauffement et coordination
                                • 11h20 — Ateliers techniques (dribble, tir statique)
                                • 12h00 — Jeux collectifs et match 3 contre 3
                                • 12h45 — Retour au calme et débrief
                                
                                Ballons et chasubles fournis. Encadrement par un éducateur sportif diplômé.
                                """,
                        """
                                • Âge : 6 à 11 ans
                                • Baskets ou chaussures de sport (semelles non marquantes)
                                • Tenue de sport et bouteille d'eau
                                • Aucun niveau requis
                                • Contre-indication : blessure récente aux chevilles ou genoux
                                """
                )),
                Map.entry("Stage cirque été", new ActivityContent(
                        """
                                Une semaine pour goûter aux arts du cirque dans un chapiteau dédié !
                                
                                Activités au choix (rotation quotidienne) :
                                • Jonglage et équilibre sur fil
                                • Acrobatie au sol et roue allemande
                                • Aerial silk initiation (harnais de sécurité)
                                • Clownerie et expression scénique
                                
                                Fin de stage : numéro collectif devant les parents (jeudi 16h).
                                Horaires : 10h-17h, garderie possible 8h30-18h (+20 €/semaine).
                                """,
                        """
                                • Âge : 7 à 12 ans
                                • Certificat médical obligatoire
                                • Tenue sportive ample, chaussons ou chaussettes antidérapantes
                                • Repas fournis ou pique-nique (à préciser à l'inscription)
                                • Pas de vertige sévère pour le fil (alternative proposée)
                                """
                )),
                Map.entry("Spectacle de marionnettes", new ActivityContent(
                        """
                                Créer sa marionnette, apprendre à la manipuler, puis monter un mini-spectacle !
                                
                                Déroulement (3h) :
                                • 15h00 — Fabrication de la marionnette (chaussette + décor)
                                • 16h00 — Atelier voix et gestuelle
                                • 16h45 — Répétition d'une saynète de 5 minutes
                                • 17h30 — Représentation devant les parents (entrée libre)
                                
                                Tout le matériel créatif est inclus. Les marionnettes sont emportées à la maison.
                                """,
                        """
                                • Âge : 5 à 10 ans
                                • Accompagnement parental souhaité pour les moins de 7 ans
                                • Tenue confortable
                                • Prévenir en cas d'allergie au latex (élastiques alternatifs disponibles)
                                """
                )),
                Map.entry("Randonnée nature", new ActivityContent(
                        """
                                Une matinée dehors pour observer, écouter et jouer avec la nature.
                                
                                Parcours de 4 km adapté aux enfants (dénivelé faible) :
                                • Identification des arbres et des traces d'animaux
                                • Jeux de collecte responsable (feuilles, pierres)
                                • Pause goûter au bord du ruisseau
                                • Atelier « cabane de fortune » en fin de parcours
                                
                                Guide naturaliste certifié. Groupe de 14 enfants maximum.
                                """,
                        """
                                • Âge : 6 à 12 ans
                                • Chaussures de randonnée ou baskets robustes
                                • K-way, casquette et crème solaire
                                • Goûter et bouteille d'eau (1,5 L recommandé)
                                • Autorisation de sortie signée
                                • En cas de pluie forte : report ou activité en salle (notification la veille)
                                """
                ))
        );
    }

    private record ActivityContent(String details, String prerequisites) {}
}
