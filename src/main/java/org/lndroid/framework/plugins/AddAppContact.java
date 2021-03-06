package org.lndroid.framework.plugins;

import java.io.IOException;
import java.lang.reflect.Type;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.DefaultPlugins;
import org.lndroid.framework.common.IPluginData;
import org.lndroid.framework.engine.PluginContext;

// Action
public class AddAppContact extends ActionBase<WalletData.Contact, WalletData.Contact> {

    private static int DEFAULT_TIMEOUT = 60000; // 60 sec
    private static int MAX_TIMEOUT = 300000; // 5 min

    @Override
    protected int defaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    protected int maxTimeout() {
        return MAX_TIMEOUT;
    }

    @Override
    protected boolean isUserPrivileged(WalletData.Contact req, WalletData.User user) {
        return user.isRoot();
    }

    @Override
    protected WalletData.Contact createResponse(PluginContext ctx, WalletData.Contact req, int authUserId) {
        return req.toBuilder()
                .setUserId(ctx.user.id())
                .setCreateTime(System.currentTimeMillis())
                .setAuthUserId(authUserId)
                .build();
    }

    @Override
    protected void signal(WalletData.Contact rep) {
        engine().onSignal(id(), DefaultTopics.NEW_CONTACT, rep);
        engine().onSignal(id(), DefaultTopics.CONTACT_STATE, rep);
    }

    @Override
    protected Type getResponseType() {
        return WalletData.Contact.class;
    }

    @Override
    protected boolean isValidUser(WalletData.User user) {
        return user.isApp();
    }

    @Override
    protected WalletData.Contact getData(IPluginData in) {
        in.assignDataType(WalletData.AddAppContactRequest.class);
        try {
            WalletData.AddAppContactRequest r = in.getData();
            // FIXME if user provides some options we'd have to store them somewhere

            // empty add request for now
            return WalletData.Contact.builder().build();
        } catch (IOException e) {
            return null;
        }
    }

    public AddAppContact() {
    }

    @Override
    public String id() {
        return DefaultPlugins.ADD_CONTACT_APP;
    }

}