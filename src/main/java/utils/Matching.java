package utils;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.*;

public class Matching {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "des", "de", "du", "la", "le", "les", "un", "une", "et", "avec"
    ));

    // Pondération pour combiner les différentes mesures de similarité
    private static final double SEMANTIC_WEIGHT = 0.5;
    private static final double SYNTAX_WEIGHT = 0.3;
    private static final double EDIT_WEIGHT = 0.2;

    public double calculateMatchPercentage(String requiredSkillsStr, String candidateSkillsStr) {
        if (requiredSkillsStr == null || candidateSkillsStr == null) {
            return 0.0;
        }

        List<String> requiredSkills = preprocessSkills(requiredSkillsStr);
        List<String> candidateSkills = preprocessSkills(candidateSkillsStr);

        if (requiredSkills.isEmpty()) return 0.0;

        double totalScore = 0.0;

        for (String required : requiredSkills) {
            double maxSimilarity = 0.0;
            for (String candidate : candidateSkills) {
                double similarity = calculateCombinedSimilarity(required, candidate);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    // Early exit if perfect match found
                    if (maxSimilarity >= 0.99) break;
                }
            }
            totalScore += maxSimilarity;
        }

        return (totalScore / requiredSkills.size()) * 100.0;
    }

    private List<String> preprocessSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.split(","))
                .map(this::normalizeSkill)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeSkill(String skill) {
        if (skill == null) {
            return "";
        }

        // Normalisation plus poussée
        String normalized = Normalizer.normalize(skill.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9\\s]", "") // Supprime la ponctuation
                .replaceAll("\\s+", " "); // Espaces multiples -> un seul

        // Suppression des stop words plus efficace
        String[] words = normalized.split(" ");
        StringBuilder filtered = new StringBuilder();
        for (String word : words) {
            if (!STOP_WORDS.contains(word)) {
                filtered.append(word).append(" ");
            }
        }

        return filtered.toString().trim();
    }

    private double calculateCombinedSimilarity(String skill1, String skill2) {
        // 1. Similarité sémantique (utilisant des embeddings ou WordNet si intégré)
        double semanticSimilarity = calculateSemanticSimilarity(skill1, skill2);

        // 2. Similarité syntaxique (Jaccard, cosinus, etc.)
        double syntacticSimilarity = calculateSyntacticSimilarity(skill1, skill2);

        // 3. Similarité basée sur la distance d'édition
        double editSimilarity = calculateEditSimilarity(skill1, skill2);

        // Combinaison pondérée des similarités
        return (semanticSimilarity * SEMANTIC_WEIGHT) +
                (syntacticSimilarity * SYNTAX_WEIGHT) +
                (editSimilarity * EDIT_WEIGHT);
    }

    private double calculateSemanticSimilarity(String skill1, String skill2) {
        // Implémentation simplifiée - idéalement utiliser Word2Vec/BERT
        Set<String> words1 = new HashSet<>(Arrays.asList(skill1.split(" ")));
        Set<String> words2 = new HashSet<>(Arrays.asList(skill2.split(" ")));

        // Similarité Jaccard comme approximation
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private double calculateSyntacticSimilarity(String skill1, String skill2) {
        // Utilisation de la similarité cosinus
        Map<String, Integer> vector1 = buildTermFrequencyVector(skill1);
        Map<String, Integer> vector2 = buildTermFrequencyVector(skill2);

        Set<String> terms = new HashSet<>();
        terms.addAll(vector1.keySet());
        terms.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : terms) {
            int v1 = vector1.getOrDefault(term, 0);
            int v2 = vector2.getOrDefault(term, 0);

            dotProduct += v1 * v2;
            norm1 += Math.pow(v1, 2);
            norm2 += Math.pow(v2, 2);
        }

        return norm1 == 0 || norm2 == 0 ? 0.0 :
                dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double calculateEditSimilarity(String skill1, String skill2) {
        // Utilisation de la distance de Levenshtein normalisée
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(skill1, skill2);
        int maxLength = Math.max(skill1.length(), skill2.length());

        return maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);
    }

    private Map<String, Integer> buildTermFrequencyVector(String text) {
        return Arrays.stream(text.split(" "))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toMap(
                        word -> word,
                        word -> 1,
                        Integer::sum
                ));
    }
}