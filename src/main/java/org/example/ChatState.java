package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatState {
    String state;
    String data = null;
    Map<String, List<Integer>> expenses = new HashMap<>();

    public String getFormattedCategories() {
        Set<String> categories = expenses.keySet();
        if (categories.isEmpty()) return "Категории отсутствуют";
        return String.join("\n", categories);
    }

    public String getFormattedExpenses() {
        if (expenses.isEmpty()) return "Категории отсутствуют";

        String formattedResult = "";
//      for (var category: expenses.entrySet())
        for (Map.Entry<String, List<Integer>> category: expenses.entrySet()) {
            String categoryName = category.getKey();
            List<Integer> categoryExpenses = category.getValue();
            formattedResult += categoryName + ": " + getFormattedExpenses(categoryExpenses) + "\n";
        }
        return formattedResult;
    }

    private String getFormattedExpenses(List<Integer> expensesPerCategory) {
        String formattedResult = "";
        for (Integer expense: expensesPerCategory) {
            formattedResult += expense + " ";
        }
        return formattedResult;
    }
}
