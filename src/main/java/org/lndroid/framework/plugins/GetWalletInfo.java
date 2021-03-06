package org.lndroid.framework.plugins;

import java.io.IOException;
import java.lang.reflect.Type;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.WalletDataDecl;
import org.lndroid.framework.common.DefaultPlugins;
import org.lndroid.framework.dao.IWalletInfoDao;
import org.lndroid.framework.common.IPluginData;
import org.lndroid.framework.engine.IPluginForegroundCallback;
import org.lndroid.framework.engine.IPluginServer;
import org.lndroid.framework.engine.PluginContext;

public class GetWalletInfo extends GetBase<Long> {

    private static final long DEFAULT_TIMEOUT = 3600000; // 1h

    private IWalletInfoDao dao_;

    public GetWalletInfo() {
        super(DefaultPlugins.GET_WALLET_INFO, DefaultTopics.WALLET_INFO);
    }

    @Override
    public void init(IPluginServer server, IPluginForegroundCallback callback) {
        super.init(callback);
        dao_ = (IWalletInfoDao) server.getDaoProvider().getPluginDao(id());
    }

    @Override
    protected long defaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    protected boolean isUserPrivileged(PluginContext ctx, WalletDataDecl.GetRequestTmpl req, WalletData.User user) {
        // FIXME or anyone who has proper limits
        return user.isRoot();
    }

    @Override
    protected Object get(Long id) {
        return dao_.get();
    }

    @Override
    protected Type getType() {
        return WalletData.WalletInfo.class;
    }

    @Override
    protected WalletData.GetRequestLong getInputData(IPluginData in) {
        in.assignDataType(WalletData.GetRequestLong.class);
        try {
            return in.getData();
        } catch (IOException e) {
            return null;
        }
    }
}
