package bot;

import config.BotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.FileService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TelegramBot extends TelegramLongPollingBot {

    private final Map<String, String> pathStorage = new HashMap<>();
    private final Map<Long, Integer> userInlineMessageId = new HashMap<>();

    @Override
    public String getBotUsername() {
        return BotConfig.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return BotConfig.getBotToken();
    }

    private ReplyKeyboardMarkup createBottomKeyBoard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("/меню"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setIsPersistent(true);
        keyboardMarkup.setSelective(false);

        return keyboardMarkup;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    sendWelcomeMessage(chatId);
                    break;
                case "/menu":
                    sendMenu(chatId);
                    break;
                case "/topics":
                    sendFolderContent(chatId, BotConfig.getRootFolder());
                    break;
                case "/answers":
                    sendFolderContent(chatId, BotConfig.getAnswerFolder());
                    break;
                default:
                    sendTextMessage(chatId, "Неверная комнада!\n Воспользуйся /menu");
            }
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void sendMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Меню:\n " +
                "/start - запуск бота\n" +
                "/topics - список доступных тем\n" +
                "/answers - билеты по истории");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Привет! Давай учить историю! Используй /menu");
        message.setReplyMarkup(createBottomKeyBoard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendFolderContent(long chatId, String folderPath) {
        if (!FileService.pathExists(folderPath)) {
            sendTextMessage(chatId, "Материал отсутствует(");
            return;
        }

        List<File> folders = FileService.getFolders(folderPath);
        List<File> files = FileService.getFiles(folderPath);

        if (folders.isEmpty() && files.isEmpty()) {
            sendTextMessage(chatId, "Здесь пока ничего нет");
            return;
        }

        if (!files.isEmpty()) {
            Collections.sort(files);
            sendFiles(chatId, files);
        }

        if (!folders.isEmpty()) {
            Collections.sort(folders);
            sendFolderButtons(chatId, folders);
        }
    }

    private void sendFolderButtons(long chatId, List<File> folders) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 0; i < folders.size(); i++) {
            String shortId = UUID.randomUUID().toString().substring(0, 8);
            pathStorage.put(shortId, folders.get(i).getAbsolutePath());

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(folders.get(i).getName());
            button.setCallbackData("f:" + shortId);
            row.add(button);

            if ((i + 1) % 3 == 0 || i == folders.size() - 1) {
                keyboard.add(new ArrayList<>(row));
                row.clear();
            }
        }

        markup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите билет!");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendFiles(long chatId, List<File> files) {
        for (File file : files) {
            if (FileService.isImage(file)) {
                sendPhoto(chatId, file);
            } else {
                sendDocument(chatId, file);
            }
        }
    }

    private void sendPhoto(long chatId, File photo) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(photo));
        sendPhoto.setCaption(photo.getName());//???

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDocument(long chatId, File document) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));
        sendDocument.setDocument(new InputFile(document));
        sendDocument.setCaption(document.getName());

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        if (callbackData.startsWith("f:")) {
            String shortId = callbackData.substring(2);
            String fullPath = pathStorage.get(shortId);

            if (fullPath != null) {
                sendFolderContent(chatId, fullPath);
            } else {
                sendTextMessage(chatId, "Папка не найдена!");
            }
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage delMessage = new DeleteMessage(String.valueOf(chatId), messageId);
        try{
            execute(delMessage);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

}
