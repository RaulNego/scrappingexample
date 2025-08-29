package com.example.util;

import java.util.*;
import java.util.regex.*;

public class ProximitySearch {

    public static class Match {
        String word;
        int position;

        public Match(String word, int position) {
            this.word = word;
            this.position = position;
        }
    }

    public static class ClosestResult {
        String phone;
        String social;
        String email;
        int distance;

        public ClosestResult(String phone, String social, String email, int distance) {
            this.phone = phone;
            this.social = social;
            this.email = email;
            this.distance = distance;
        }

        public String getPhone() { return phone; }
        public String getSocial() { return social; }
        public String getEmail() { return email; }
        public int getDistance() { return distance; }
    }

    public static ClosestResult findClosestProximity(String text, List<String> phones, List<String> socials, List<String> emails) {
        text = text.toLowerCase();

        List<Match> occurrencesPhones = (phones != null) ? findOccurrences(text, phones) : Collections.emptyList();
        List<Match> occurrencesSocials = (socials != null) ? findOccurrences(text, socials) : Collections.emptyList();
        List<Match> occurrencesEmails = (emails != null) ? findOccurrences(text, emails) : Collections.emptyList();

        int nonEmptyCount = 0;
        if (!occurrencesPhones.isEmpty()) nonEmptyCount++;
        if (!occurrencesSocials.isEmpty()) nonEmptyCount++;
        if (!occurrencesEmails.isEmpty()) nonEmptyCount++;

        if (nonEmptyCount < 2) {
            return null;
        }

        ClosestResult closestResult = null;
        int minDistance = Integer.MAX_VALUE;

        if (nonEmptyCount == 2) {
            if (occurrencesEmails.isEmpty()) {
                closestResult = compareTwoLists(occurrencesPhones, occurrencesSocials, "phone", "social");
            } else if (!occurrencesPhones.isEmpty()) {
                closestResult = compareTwoLists(occurrencesPhones, occurrencesEmails, "phone", "email");
            } else {
                closestResult = compareTwoLists(occurrencesSocials, occurrencesEmails, "social", "email");
            }
        } else {
            for (Match phone : occurrencesPhones) {
                for (Match social : occurrencesSocials) {
                    for (Match email : occurrencesEmails) {
                        int maxDistance = maxAbs(phone.position, social.position, email.position);
                        if (maxDistance < minDistance) {
                            minDistance = maxDistance;
                            closestResult = new ClosestResult(phone.word, social.word, email.word, minDistance);
                        }
                    }
                }
            }
        }

        return closestResult;
    }

    private static ClosestResult compareTwoLists(List<Match> list1, List<Match> list2, String label1, String label2) {
        int minDistance = Integer.MAX_VALUE;
        String word1 = null, word2 = null;
        for (Match m1 : list1) {
            for (Match m2 : list2) {
                int distance = Math.abs(m1.position - m2.position);
                if (distance < minDistance) {
                    minDistance = distance;
                    word1 = m1.word;
                    word2 = m2.word;
                }
            }
        }

        String phone = null, social = null, email = null;
        if (label1.equals("phone")) phone = word1;
        if (label1.equals("social")) social = word1;
        if (label1.equals("email")) email = word1;

        if (label2.equals("phone")) phone = word2;
        if (label2.equals("social")) social = word2;
        if (label2.equals("email")) email = word2;

        return new ClosestResult(phone, social, email, minDistance);
    }

    private static int maxAbs(int a, int b, int c) {
        int maxPos = Math.max(a, Math.max(b, c));
        int minPos = Math.min(a, Math.min(b, c));
        return maxPos - minPos;
    }

    private static List<Match> findOccurrences(String text, List<String> words) {
        List<Match> occurrences = new ArrayList<>();
        for (String word : words) {
            String lowerWord = word.toLowerCase();
            Matcher matcher = Pattern.compile(Pattern.quote(lowerWord)).matcher(text);
            while (matcher.find()) {
                occurrences.add(new Match(word, matcher.start()));
            }
        }
        return occurrences;
    }
}
