package ru.gosuslugi.geps.robot;


import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ApiResponse;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpClient;
import io.vertx.mutiny.core.http.HttpClientResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.geps.robot.db.BotSettings;
import ru.gosuslugi.geps.robot.db.ChatOption;
import ru.gosuslugi.geps.robot.db.UserChatPK;
import ru.gosuslugi.geps.robot.db.UserChatSettings;

@ApplicationScoped
@Startup
@Slf4j
public class TelegramService {
//    private TelegramClient telegramClient;

    @ConfigProperty(name = "geps-bot.token")
    String token;

    private final HttpClient httpClient;
//    private ObjectMapper objectMapper = new ObjectMapper();

    public TelegramService(Vertx vertx) {
        Objects.requireNonNull(vertx);

        HttpClientOptions httpClientOptions = new HttpClientOptions();
//        httpClientOptions.getDefaultHost()
        httpClient = vertx.createHttpClient(httpClientOptions);
        log.info("Telegram service created");
    }

    @Scheduled(every = "100s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    Uni<Void> init() {
        log.info("Scheduled update check");
        return processUpdates("scheduler")
                .onItem().transformToUni(l -> l > 0 ? processUpdates("schedule2") : Uni.createFrom().item(0))
                .replaceWithVoid()
                .onFailure().recoverWithNull();
    }

    public Uni<String> checkUpdates() {
        return processUpdates("web")
                .replaceWith("Done")
                .onFailure().recoverWithItem("error");
    }

    private Uni<Integer> processUpdates(String source) {
        log.info("Check updates: {}", source);
        return loadTelegramUpdates()
                .onItem().transformToMulti(r -> Multi.createFrom().iterable(r))
                .onItem().transformToUni(this::processUpdate)
                .merge().collect().asList()
                .onItem().transform(List::size)
                .onFailure().invoke(e -> log.error("Exception occurred in [{}]: ", source, e));
    }

/*
    private Uni<UpdateContext> processUpdates(UpdateContext ctx) {
        Set<Long> notifyChatIds = new HashSet<>();
        Set<UserChatPK> userChatIds = new HashSet<>();
        ctx.getResponse().getResult().forEach(u -> {
            Message message = u.getMessage();
            if (message != null) {

//                        message.getFrom();
                notifyChatIds.add(message.getChat().getId());
            }
        });

        UserChatSettings.list("chatIds in {}", notifyChatIds);
    }
*/

    private Uni<Void> processUpdate(Update update) {
        Message message;
        List<MessageEntity> entities;
        if ((message = update.getMessage()) == null || (entities = update.getMessage().getEntities()) == null) {
            return Uni.createFrom().voidItem();
        }
        boolean notify = false;
        for (MessageEntity entity : entities) {
            if (entity.getType().equals("hashtag") && entity.getText().equals("#all")) {
                notify = true;
            } else if (entity.getType().equals("bot_command")) {
                return message.getFrom().getId().equals(message.getChatId())
                        ? processBotCommandUser(update, message, entity)
                        : processBotCommandChat(update, message, entity);
            }
        }
        if (notify) {
            return notifyUsers(update, message);
        } else {
            return Uni.createFrom().voidItem();
        }
    }

    private Uni<Void> notifyUsers(Update update, Message message) {
        log.info("Search chat users ");
        return UserChatSettings.<UserChatSettings>list("chatId=?1", message.getChatId())
                .onItem().transformToMulti(u -> Multi.createFrom().iterable(u))
                .onItem().transformToUni(u -> {
                    if (u.options.contains(ChatOption.notify)) {
//                        return sendNotifyMessage(u.userId, message.getText());
                        return forwardMessage(message, u.userId);
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                })
                .merge().collect().asList()
                .replaceWithVoid();
    }

    private Uni<Message> forwardMessage(Message message, long userId) {
        return telegram(ForwardMessage.builder()
                .chatId(userId)
                .fromChatId(message.getChatId())
                .messageId(message.getMessageId())
                .build());
//        TypeReference<ApiResponse<Message>> a = new TypeReference<>(){};
    }

    private Uni<Message> sendMessage(long userId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId)
                .text(text)
                .build();
//        TypeReference<ApiResponse<Message>> a = new TypeReference<>(){};
        Uni<Message> response = telegram(sendMessage);
        return response;
    }

    private Uni<Void> processBotCommandUser(Update update, Message message, MessageEntity entity) {
        SendMessage sendMenuMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text("Select action")
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text("Show notifications")
                                        .callbackData("notifications")
                                        .build()
                        ))
                        .build())
                .build();

        return
                telegram(sendMenuMessage)
                .replaceWithVoid();
    }

    private Uni<Void> processBotCommandChat(Update update, Message message, MessageEntity entity) {
        EditMessageText editBotCommandMessage = EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text("Got you")
                .build();

        SendMessage sendMenuMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text("Select action")
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text("Чат с ботом")
//                                        .url("tg://user?id=geps-robot")
//                                        .url("https://t.me/geps-robot?start=setup")
                                        .url("https://t.me/geps_robot")
                                        .build()
                        ))
                        .build())
                .build();

        return
/*
                UserChatSettings.<UserChatSettings>findById(new UserChatPK(UserChatSettings.NO_ITEM, message.getChatId()))
                        .onItem().transformToUni(s -> {
                            Context<>
                            if (s != null) {

                            }
                })
                        .onItem().transformToUni(c -> telegram(editBotCommandMessage))
*/

                telegram(editBotCommandMessage)
                .onFailure().recoverWithItem(e -> {
                    log.error("Error process item: {}", e.toString());
                    return null;
                })
                .onItem().transformToUni(r -> telegram(sendMenuMessage))
                .replaceWithVoid();
    }

    private Uni<ArrayList<Update>> loadTelegramUpdates() {
        return BotSettings.<BotSettings>findById(1)
                .onItem().transform(s -> s == null ? 0 : s.lastUpdate + 1)
                .onItem().invoke(l -> log.info("Start telegram updates check [{}] ...", l))
                .onItem().transformToUni(l -> telegram(GetUpdates.builder()
                        .offset(l)
                        .timeout(100)
                        .build()))
                .onItem().transformToUni(r -> {
                    log.debug("Telegram update received: {}", r);
                    Integer newLastUpdate = r.stream()
                            .peek(u -> log.debug("    {}", u))
                            .map(Update::getUpdateId)
                            .max(Integer::compareTo)
                            .orElse(0);
                    if (newLastUpdate == 0) {
                        return Uni.createFrom().item(r);
                    } else {
                        BotSettings settings = new BotSettings();
                        settings.lastUpdate = newLastUpdate;
                        return Panache.getSession()
                                .onItem().transformToUni(s -> s.merge(settings))
                                .replaceWith(r);
                    }
                });
    }

    private <T extends Serializable> Uni<T> telegram(BotApiMethod<T> method) {
//        WebClient webClient;
//        HttpClientRequest.newInstance()
//        request.putHeader();
        new RequestOptions()
                .setHost("api.telegram.org")
                .setPort(443)
                .setMethod(HttpMethod.POST)
                .setURI("https://api.telegram.org/bot" + token + "/" + method.getMethod())
                .setSsl(true);
        return httpClient.request(new RequestOptions()
                        .setHost("api.telegram.org")
                        .setPort(443)
                        .setMethod(HttpMethod.POST)
                        .setURI("https://api.telegram.org/bot" + token + "/" + method.getMethod())
                        .setSsl(true))
//        return httpClient.request(HttpMethod.POST, 443, "api.telegram.org", "https://api.telegram.org/bot" + token + "/" + method.getMethod())
//        return httpClient.request(HttpMethod.POST, "https://api.telegram.org/bot" + token + "/" + method.getMethod())
//        return httpClient.request(HttpMethod.POST, "http://localhost:8081/bot" + token + "/" + method.getMethod())
                .onItem().invoke(r -> {
                    r.putHeader("content-type", "application/json");
//                    r.setTimeout(5000);
                })
                .onItem().transformToUni(r -> {
                    String request = Json.encode(method);
                    log.info("Telegram request [{}]: {}", /*r.getHost(), r.getPort(), r.getMethod(),*/ method.getMethod(), method);
//                    r.headers().forEach((k, v) -> log.debug("    Header '{}': {}", k, v));
//                    log.info("Request: {}", request);
                    return r.send(request);
                })
                .onItem().invoke(resp -> {
                    if (resp.statusCode() != HttpStatus.SC_OK) {
                        log.warn("Error {}: {} for {}", resp.statusCode(), resp.statusMessage(), method);

                        resp.headers().forEach((k, v) -> log.debug("    Header '{}': {}", k, v));
//                        throw new RuntimeException("Error");
                    }
                })
                .onItem().transformToUni(HttpClientResponse::body)
                .onItem().transform(b -> {
                    String body = b.toString();
                    try {
                        return method.deserializeResponse(body);
                    } catch (TelegramApiRequestException e) {
                        log.error("Telegram reported error[{}]: {}", method.getMethod(), e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
    }

    @Data
    @Builder
    static class Context<T> {
        private BotSettings botSettings;
        private UserChatSettings chatSettings;
        private ApiResponse<T> response;
    }
}
