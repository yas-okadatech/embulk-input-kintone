package org.embulk.input.kintone;

import com.cybozu.kintone.client.authentication.Auth;
import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.model.record.GetRecordsResponse;
import com.cybozu.kintone.client.module.record.Record;
import org.embulk.config.ConfigException;
import org.embulk.spi.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class KintoneClient {
    private final Logger logger = LoggerFactory.getLogger(KintoneClient.class);
    private Auth kintoneAuth;
    private Record kintoneRecordManager;
    private Connection con;

    public KintoneClient(){
        this.kintoneAuth = new Auth();
    }

    public void validateAuth(final PluginTask task) throws ConfigException{
        if (task.getUsername().isPresent() && task.getPassword().isPresent()) {
            return;
        } else if (task.getToken().isPresent()) {
            return;
        } else {
            throw new ConfigException("Username and password or token must be provided");
        }
    }

    public void connect(final PluginTask task) {
        if (task.getUsername().isPresent() && task.getPassword().isPresent()) {
            this.kintoneAuth.setPasswordAuth(task.getUsername().get(), task.getPassword().get());
        } else if (task.getToken().isPresent()) {
            this.kintoneAuth.setApiToken(task.getToken().get());
        }

        if (task.getBasicAuthUsername().isPresent() && task.getBasicAuthPassword().isPresent()) {
            this.kintoneAuth.setBasicAuth(task.getBasicAuthUsername().get(),
                    task.getBasicAuthPassword().get());
        }

        if (task.getGuestSpaceId().isPresent()) {
            this.con = new Connection(task.getDomain(), this.kintoneAuth, task.getGuestSpaceId().or(-1));
        } else {
            this.con = new Connection(task.getDomain(), this.kintoneAuth);
        }
        this.kintoneRecordManager = new Record(con);
    }


    public GetRecordsResponse getResponse(final PluginTask task) {
        ArrayList<String> fields = new ArrayList<>();
        for (ColumnConfig c : task.getFields().getColumns()
        ) {
            fields.add(c.getName());
        }
        try {
            return kintoneRecordManager.getAllRecordsByQuery(
                    task.getAppId(), task.getQuery().or(""), fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
