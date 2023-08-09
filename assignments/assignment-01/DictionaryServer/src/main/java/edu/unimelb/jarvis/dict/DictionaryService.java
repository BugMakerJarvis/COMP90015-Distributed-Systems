package edu.unimelb.jarvis.dict;

import edu.unimelb.jarvis.DictionaryServerApp;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DictionaryService {

    private static volatile List<Word> dictionaryArray;
    private static volatile long maxId = 0;

    public static boolean loadDictionary(String path) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray;
            if ("/dictionary.json".equals(path)) {
                InputStream inputStream = DictionaryServerApp.class.getResourceAsStream(path);
                jsonArray = (JSONArray) parser.parse(new InputStreamReader(inputStream));
            } else {
                jsonArray = (JSONArray) parser.parse(new FileReader(path));
            }
            List<Word> temp = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                Long id = (Long) jsonObject.get("id");
                if (id > maxId) {
                    maxId = id;
                }
                String word = (String) jsonObject.get("name");
                String meaning = (String) jsonObject.get("description");
                temp.add(new Word(id, word, meaning));
            }
            setDictionary(temp);
            return true;
        } catch (Exception e) {
            log.info("Error loading dictionary");
            return false;
        }
    }

    public static List<Word> getDictionary() {
        return new ArrayList<>(dictionaryArray);
    }

    public static synchronized void setDictionary(List<Word> dictionaryArray) {
        DictionaryService.dictionaryArray = dictionaryArray;
    }


    public static synchronized long getMaxId() {
        return maxId;
    }

    public static synchronized void setMaxId(long id) {
        maxId = id;
    }

    public static synchronized String addWord(String word, String meaning) {
        List<Word> dictionary = getDictionary();
        for (Word w : dictionary) {
            if (w.getWord().equals(word)) {
                return "REPEAT";
            }
        }
        long newId = getMaxId() + 1;
        dictionary.add(new Word(newId, word, meaning));
        setDictionary(dictionary);
        setMaxId(newId);
        return "SUCCESS";
    }

    public static synchronized String deleteWord(Long id) {
        List<Word> dictionary = getDictionary();
        boolean b = dictionary.removeIf(word -> word.getId().equals(id));
        if (!b) {
            return "NOT_FOUND";
        } else {
            setDictionary(dictionary);
            return "SUCCESS";
        }
    }

    public static synchronized String updateWord(Long id, String newWord, String newMeaning) {
        List<Word> dictionary = getDictionary();
        for (int i = 0; i < dictionary.size(); i++) {
            Word oldWord = dictionary.get(i);
            if (oldWord.getId().equals(id)) {
                dictionary.set(i, new Word(id, newWord, newMeaning));
                setDictionary(dictionary);
                return "SUCCESS";
            }
        }
        return "NOT_FOUND";
    }

    public static synchronized List<Word> searchWord(String word) {
        List<Word> dictionary = getDictionary();
        List<Word> result = new ArrayList<>();
        for (Word w : dictionary) {
            if (w.getWord().equals(word)) {
                result.add(w);
            }
        }
        return result;
    }
}
