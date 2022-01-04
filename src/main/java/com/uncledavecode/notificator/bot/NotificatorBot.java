package com.uncledavecode.notificator.bot;

import java.time.LocalDateTime;

import com.uncledavecode.notificator.config.BotConfiguration;
import com.uncledavecode.notificator.controllers.UserController;
import com.uncledavecode.notificator.model.AccessRequest;
import com.uncledavecode.notificator.model.Step;
import com.uncledavecode.notificator.model.UserAccount;
import com.uncledavecode.notificator.services.AccessRequestService;
import com.uncledavecode.notificator.utils.BotUtils;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class NotificatorBot extends TelegramLongPollingBot {

    private final BotConfiguration botConfiguration;
    private final AccessRequestService accessRequestService;
    private final UserController userController;

    public NotificatorBot(BotConfiguration botConfiguration, AccessRequestService accessRequestService,
            UserController userController) {
        this.botConfiguration = botConfiguration;
        this.accessRequestService = accessRequestService;
        this.userController = userController;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null) {
            if (update.hasMessage()) {
                this.processMessage(update);
            } else if (update.hasCallbackQuery()) {
                this.processCallBack(update);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfiguration.getUsername();
    }

    @Override
    public String getBotToken() {
        return this.botConfiguration.getToken();
    }

    private void processMessage(Update update) {
        Message message = update.getMessage();
        if (message.getText() != null) {
            if (message.getText().equalsIgnoreCase("/start")) {
                this.processStartCommand(message);
            } else if (message.getText().equalsIgnoreCase("/register")) {
                this.processRegisterCommand(message);
            } else {
                AccessRequest request = this.accessRequestService.getByChatId(message.getChatId());
                if (request != null) {
                    this.continueProcessingUser(request, update);
                } else {
                    UserAccount user = this.userController.getUserAccountByChatId(message.getChatId());
                    if (user == null) {
                        BotUtils.sendMessage(this,
                                "To continue, you must register\\.\nRun the \\/register command to continue\\.",
                                message.getChatId(), ParseMode.MARKDOWNV2);
                    } else {
                        BotUtils.sendMessage(this, "You are alredy registered\\.", message.getChatId(),
                                ParseMode.MARKDOWNV2);
                    }
                }
            }
        }
    }

    private void processCallBack(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {

            Long chat_id = update.getCallbackQuery().getMessage().getChatId();

            AccessRequest request = accessRequestService.getByChatId(chat_id);
            if (request != null) {
                String response = update.getCallbackQuery().getData();

                String answer = "";

                if (response.equalsIgnoreCase("yes")) {
                    this.userController.addUserAccount(request);
                    answer = "Process Completed\\!\nUser account is pending for approval";
                } else {
                    this.accessRequestService.deleteByChatId(request.getChatId());
                    answer = "Registration canceled\\!";
                }

                BotUtils.sendCallBackMessage(this, update.getCallbackQuery().getMessage(), answer);
            }
        }
    }

    private void processStartCommand(Message message) {
        BotUtils.sendMessage(this, "Welcome to Uncle Dave's Bot!!!", message.getChatId());
    }

    private void processRegisterCommand(Message message) {
        AccessRequest request = this.accessRequestService.getByChatId(message.getChatId());
        if (request == null) {
            request = new AccessRequest();
            request.setChatId(message.getChatId());
            request.setLogid(message.getFrom().getUserName());
            request.setRequestDate(LocalDateTime.now());
            request.setStep(Step.EMAIL);
            
            this.accessRequestService.updateAccessRequest(request);

            BotUtils.sendMessage(this, "Starting user registration.!!!", request.getChatId());
        } else {
            BotUtils.sendMessage(this, "We continue the user registration.!!!", request.getChatId());
        }

        this.showMessageFromStep(request);
    }

    private void showMessageFromStep(AccessRequest request) {
        switch (request.getStep()) {
            case EMAIL:
                BotUtils.sendMessage(this, "Step 1\nEnter an Email\\.", request.getChatId(), ParseMode.MARKDOWNV2);
                break;
            case NAME:
                BotUtils.sendMessage(this, "Step 2\nEnter Name\\.", request.getChatId(), ParseMode.MARKDOWNV2);
                break;
            case LASTNAME:
                BotUtils.sendMessage(this, "Step 2\nEnter Last Name\\.", request.getChatId(), ParseMode.MARKDOWNV2);
                break;
            case FINISH:
                BotUtils.sendYesNoQuestion(this, "Final Step\nCreate User Account?", request.getChatId());
                break;
            default:
                break;
        }
    }

    private void continueProcessingUser(AccessRequest request, Update update) {
        if (request != null && update != null) {
            if (request.getStep() != Step.START && request.getStep() != Step.FINISH) {
                boolean isValid = true;
                String textValue = update.getMessage().getText();
                switch (request.getStep()) {
                    case EMAIL:
                        isValid = EmailValidator.getInstance().isValid(textValue);
                        if (isValid) {
                            request.setEmail(textValue);
                            request.setStep(Step.NAME);
                        } else {
                            BotUtils.sendMessage(this, "Email not valid", request.getChatId());
                        }
                        break;
                    case NAME:
                        request.setName(textValue);
                        request.setStep(Step.LASTNAME);
                        break;
                    case LASTNAME:
                        request.setLastname(textValue);
                        request.setStep(Step.FINISH);
                        break;
                    default:
                        break;
                }
                if (isValid) {
                    request = this.accessRequestService.updateAccessRequest(request);
                }
            }

            this.showMessageFromStep(request);
        }
    }
}
