package app.src.main.java.school.managemnet.system.Source.App.HeadlessConfig.UserOptions;

import java.util.List;

import app.src.main.java.school.managemnet.system.Source.App.Database.QueryAPI;
import app.src.main.java.school.managemnet.system.Source.App.HeadlessConfig.DataConfigTypes.DataTypes;
import app.src.main.java.school.managemnet.system.Source.App.MessageProtocol.MessageAPI;
import app.src.main.java.school.managemnet.system.Source.App.MessageProtocol.MessageType;
import app.src.main.java.school.managemnet.system.Source.App.UserFunctionalty.User;

public class SeeMessages implements OptionsInterface {

    @Override
    public void ExecuteOption(User user, DataTypes blob) {
        List<MessageType> messages = QueryAPI.GetMessages(user.getUserID());
        MessageAPI.ViewMessages(messages);
    }
    
}