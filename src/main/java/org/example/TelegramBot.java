package org.example;
import org.example.Config;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {

    private static final String ADD_EXPENSE_BTN = "Добавить трату";
    private static final String SHOW_CATEGORIES_BTN = "Показать категории";
    private static final String SHOW_EXPENSES_BTN = "Показать траты";

    //  Состояние: IDLE -> AWAITS_CATEGORIES -> AWAITS_EXPENSES -> IDLE
    //             IDLE -> IDLE

    private static final String IDLE_STATE = "IDLE";
    private static final String AWAITS_CATEGORY_STATE = "AWAITS_CATEGORY";
    private static final String AWAITS_EXPENSE_STATE = "AWAITS_EXPENSE";

    private static String currentState = IDLE_STATE;
    private static String lastCategory = null;

    private static final Map<String, List<Integer>> EXPENSES = new HashMap<>();

    private static final Map<Long, ChatState> CHATS = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "hexlet_java_bot";
    }

    @Override
    public String getBotToken() {
        Config botToken = new Config();
            String token = String.valueOf(botToken.botToken());
            return token;
    }

//

//      return "7663841192:AAFBjKP3AUqvyocoKNuAhrHtYj5FRfzCgmQ";
//    "7663841192:AAFBjKP3AUqvyocoKNuAhrHtYj5FRfzCgmQ"

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println("Unsupported updated");
            return;
        }
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            CHATS.putIfAbsent(chatId, new ChatState());

            User from = message.getFrom();
            String text = message.getText();
            String logMessage = from.getUserName() + ": " + text;
            System.out.println(logMessage);

            switch (currentState) {
                case IDLE_STATE -> handleIdle(message);
                case AWAITS_CATEGORY_STATE -> handleAwaitsCategory(message);
                case AWAITS_EXPENSE_STATE -> handleAwaitsExpense(message);
            }

    }

    private void handleIdle(Message incomingMessage) {
        String incomingText = incomingMessage.getText();
        Long chatId = incomingMessage.getChatId();

        final List<String> defaultButtons = List.of(
                ADD_EXPENSE_BTN,
                SHOW_CATEGORIES_BTN,
                SHOW_EXPENSES_BTN
        );

        switch (incomingText) {
            case SHOW_CATEGORIES_BTN -> changeState(
                    IDLE_STATE,
                    chatId,
                    getFormattedCategories(),
                    defaultButtons
            );
            case SHOW_EXPENSES_BTN -> changeState(
                    IDLE_STATE,
                    chatId,
                    getFormattedExpenses(),
                    defaultButtons
            );

            case ADD_EXPENSE_BTN -> changeState(
                    AWAITS_CATEGORY_STATE,
                    chatId,
                    "Укажите категорию",
                    null
            );

            default -> changeState(
                    IDLE_STATE,
                    chatId,
                    "Я не знаю такой команды",
                    defaultButtons
            );
        }
    }

    private void handleAwaitsCategory(Message incomingMessage){
        String incomingText = incomingMessage.getText();
        Long chatId = incomingMessage.getChatId();
        EXPENSES.putIfAbsent(incomingText, new ArrayList<>());
        lastCategory = incomingText;
        changeState(
                AWAITS_EXPENSE_STATE,
                chatId,
                "Введите сумму",
                null
        );
    }

    private void handleAwaitsExpense(Message incomingMessage) {
        Long chatId = incomingMessage.getChatId();
        if (lastCategory == null) {
            changeState(
                    IDLE_STATE,
                    chatId,
                    "Что-то пошло не так. Попробуйте сначала",
                    List.of(
                            ADD_EXPENSE_BTN,
                            SHOW_CATEGORIES_BTN,
                            SHOW_EXPENSES_BTN)
            );
            return;
        }
        String incomingText = incomingMessage.getText();

        Integer expense = Integer.parseInt(incomingText);
        EXPENSES.get(lastCategory).add(expense);
        changeState(
                IDLE_STATE,
                chatId,
                "Трата успушно добавлена",
                List.of(
                        ADD_EXPENSE_BTN,
                        SHOW_CATEGORIES_BTN,
                        SHOW_EXPENSES_BTN)
        );
    }

    private void changeState(
            String newState,
            Long chatId,
            String messageText,
            List<String> buttonNames
            ) {
        System.out.println(currentState + " -> " + newState);
        currentState = newState;

        ReplyKeyboard keyboard = buildKeyboard(buttonNames);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("!!!ERROR!!!");
            System.out.println(e);
        }
    }

    private ReplyKeyboard buildKeyboard(List<String> buttonNames) {
        if (buttonNames == null || buttonNames.isEmpty()) return new ReplyKeyboardRemove(true);
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
// 1:47:00 Java_TelegramBot_2
