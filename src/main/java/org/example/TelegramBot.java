package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {

    private static final String ADD_EXPENSE_BTN = "Добавить трату";
    private static final String SHOW_CATEGORIES_BTN = "Показать категории";
    private static final String SHOW_EXPENSES_BTN = "Показать траты";

    private static final String IDLE_STATE = "IDLE";
    private static final String AWAITS_CATEGORY_STATE = "AWAITS_CATEGORY";
    private static final String AWAITS_EXPENSE_STATE = "AWAITS_EXPENSE";

    private static String currentState = IDLE_STATE;

    private static final Map<String, List<Integer>> EXPENSES = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "hexlet_java_bot";
    }

    @Override
    public String getBotToken() {
        return "7663841192:AAFBjKP3AUqvyocoKNuAhrHtYj5FRfzCgmQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println("Unsupported updated");
            return;
        }
            Message message = update.getMessage();

            User from = message.getFrom();
            String text = message.getText();
            String logMessage = from.getUserName() + ": " + text;
            System.out.println(logMessage);

            switch (currentState) {
                case IDLE_STATE -> handleIdle(message);
                case AWAITS_CATEGORY_STATE -> System.out.println(currentState);
                case AWAITS_EXPENSE_STATE -> System.out.println(currentState);
            }

    }

    private void handleIdle(Message incomingMessage) {
        String incomingText = incomingMessage.getText();
        Long chatId = incomingMessage.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        switch (incomingText) {
            case SHOW_CATEGORIES_BTN -> sendMessage.setText(getFormattedCategories());
            case SHOW_EXPENSES_BTN -> sendMessage.setText(getFormattedExpenses());
            case ADD_EXPENSE_BTN -> sendMessage.setText("Введите имя категории и сумму через пробел");

            default -> {
                String[] expense = incomingText.split(" ");
                if (expense.length == 2) {
                    String category = expense[0];
                    EXPENSES.putIfAbsent(category, new ArrayList<>());
                    Integer sum = Integer.parseInt(expense[1]);
                    EXPENSES.get(category).add(sum);
                } else {
                    sendMessage.setText("Похоже вы неверно ввели трату");
                }
            }
        }

    //  Состояние: IDLE -> AWAITS_CATEGORIES -> AWAITS_EXPENSES -> IDLE

        ReplyKeyboardMarkup keyboard = buildKeyboard(
                List.of(
                        ADD_EXPENSE_BTN,
                        SHOW_CATEGORIES_BTN,
                        SHOW_EXPENSES_BTN
                )
        );
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("!!!ERROR!!!");
            System.out.println(e);
        }
    }

    private ReplyKeyboardMarkup buildKeyboard(List<String> buttonNames) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (String buttonName: buttonNames) {
            KeyboardRow row = new KeyboardRow();
            row.add(buttonName);
            rows.add(row);
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private String getFormattedCategories() {
        Set<String> categories = EXPENSES.keySet();
        if (categories.isEmpty()) return "Пока нет ни одной категории";
        return String.join("\n", EXPENSES.keySet());
    }

    private String getFormattedExpenses() {
        Set<Map.Entry<String, List<Integer>>> expensesPerCategories =
                EXPENSES.entrySet();
        if (expensesPerCategories.isEmpty()) return "Пока нет ни одной " +
                "категории";

        String formattedResult = "";
        for (Map.Entry<String, List<Integer>> category : EXPENSES.entrySet()) {
            String categoryExpenses = "";
            for (Integer expense : category.getValue()) {
                categoryExpenses += expense + " ";
            }
            formattedResult += (category.getKey() + ": " + categoryExpenses) + "\n";
        }
        return formattedResult;
    }
}
// 34:00 Java_TelegramBot_2
