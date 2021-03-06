package org.lndroid.framework.engine;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

import org.lndroid.framework.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.PluginData;

public class AuthClient extends Handler implements IAuthClient {

    private static class Callback<T> {
        int id;
        IResponseCallback<T> cb;
        Callback(int id, IResponseCallback<T> cb) {
            this.id = id;
            this.cb = cb;
        }
    }

    private Messenger self_;
    private Messenger server_;
    private int nextCallbackId_ = 1;
    private Map<Integer, Callback<?>> callbacks_ = new HashMap<>();

/*    private Map<Integer, IResponseCallback<Boolean>> isUserPrivilegedCallbacks_ = new HashMap<>();
    private Map<Integer, IResponseCallback<Boolean>> authorizedCallbacks_ = new HashMap<>();
    private List<IAuthRequestCallback> authRequestCallbacks_ = new ArrayList<>();
    private List<IWalletStateCallback> walletStateCallbacks_ = new ArrayList<>();
    private IResponseCallback<WalletData.GenSeedResponse> genSeedCallback_;
    private IResponseCallback<WalletData.InitWalletResponse> initWalletCallback_;
    private IResponseCallback<WalletData.UnlockWalletResponse> unlockWalletCallback_;

 */

    public AuthClient(Messenger server) {
        self_ = new Messenger(this);
        server_ = server;
    }

    private void onCallback(AuthData.AuthMessage pm) {
        Callback cb = callbacks_.get(pm.id());
        if (cb == null)
            throw new RuntimeException("Auth callback not found");

        if (pm.code() != null)
            cb.cb.onError(pm.code(), pm.error());
        else
            cb.cb.onResponse(pm.data());
    }

/*    private void onAuthedMessage(AuthData.AuthMessage pm) {
        IResponseCallback<Boolean> cb = authorizedCallbacks_.remove(pm.authId());
        if (pm.code() != null)
            cb.onError(pm.code(), pm.error());
        else
            cb.onResponse(true);
    }

    private void onAuthMessage(AuthData.AuthMessage pm) {
        for(IAuthRequestCallback cb: authRequestCallbacks_) {
            cb.onAuthRequest((WalletData.AuthRequest)pm.data());
        }
    }

    private void onWalletStateMessage(AuthData.AuthMessage pm) {
        for(IWalletStateCallback cb: walletStateCallbacks_) {
            cb.onWalletState((WalletData.WalletState)pm.data());
        }
    }

    private void onGenSeedMessage(AuthData.AuthMessage pm) {
        if (pm.code() != null) {
            genSeedCallback_.onError(pm.code(), pm.error());
        } else {
            genSeedCallback_.onResponse((WalletData.GenSeedResponse)pm.data());
        }
    }

    private void onInitWalletMessage(AuthData.AuthMessage pm) {
        if (pm.code() != null) {
            initWalletCallback_.onError(pm.code(), pm.error());
        } else {
            initWalletCallback_.onResponse((WalletData.InitWalletResponse)pm.data());
        }
    }

    private void onUnlockWalletMessage(AuthData.AuthMessage pm) {
        if (pm.code() != null) {
            unlockWalletCallback_.onError(pm.code(), pm.error());
        } else {
            unlockWalletCallback_.onResponse((WalletData.UnlockWalletResponse)pm.data());
        }
    }
*/
    @Override
    public void handleMessage(Message msg) {
        AuthData.AuthMessage pm = (AuthData.AuthMessage)msg.obj;

        switch(pm.type()) {
            case AuthData.MESSAGE_TYPE_PRIV:
            case AuthData.MESSAGE_TYPE_AUTHED:
            case AuthData.MESSAGE_TYPE_AUTH_SUB:
            case AuthData.MESSAGE_TYPE_WALLET_STATE_SUB:
            case AuthData.MESSAGE_TYPE_GEN_SEED:
            case AuthData.MESSAGE_TYPE_INIT_WALLET:
            case AuthData.MESSAGE_TYPE_UNLOCK_WALLET:
            case AuthData.MESSAGE_TYPE_GET:
            case AuthData.MESSAGE_TYPE_GET_TX:
                onCallback(pm);
                break;

/*            case AuthData.MESSAGE_TYPE_AUTHED:
                onAuthedMessage(pm);
                break;

            case AuthData.MESSAGE_TYPE_AUTH_SUB:
                onAuthMessage(pm);
                break;

            case AuthData.MESSAGE_TYPE_WALLET_STATE_SUB:
                onWalletStateMessage(pm);
                break;

            case AuthData.MESSAGE_TYPE_GEN_SEED:
                onGenSeedMessage(pm);
                break;

            case AuthData.MESSAGE_TYPE_INIT_WALLET:
                onInitWalletMessage(pm);
                break;

            case AuthData.MESSAGE_TYPE_UNLOCK_WALLET:
                onUnlockWalletMessage(pm);
                break;
*/
            default:
                throw new RuntimeException("Unexpected auth client response");
        }
    }

    private void send(AuthData.AuthMessage pm) {

        Message m = this.obtainMessage(PluginData.MESSAGE_WHAT_AUTH, pm);

        // set self as client
        m.replyTo = self_;

        try {
            server_.send(m);
        } catch (RemoteException e) {
            throw new RuntimeException("Server is gone");
        }
    }

    private <T> void send(AuthData.AuthMessage.Builder b, IResponseCallback<T> cb) {
        final int id = nextCallbackId_;
        nextCallbackId_++;

        callbacks_.put(id, new Callback<T>(id, cb));

        b.setId(id);

        send(b.build());
    }

    @Override
    public void isUserPrivileged(String pluginId, int authUserId, int authId, IResponseCallback<Boolean> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(AuthData.MESSAGE_TYPE_PRIV)
                .setAuthId(authId)
                .setUserId(authUserId);
        send(b, cb);
    }

    @Override
    public void subscribeBackgroundAuthRequests(IResponseCallback<WalletData.AuthRequest> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(AuthData.MESSAGE_TYPE_AUTH_SUB);

        send(b, cb);
    }

    @Override
    public void subscribeWalletState(IResponseCallback<WalletData.WalletState> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(AuthData.MESSAGE_TYPE_WALLET_STATE_SUB);
        send(b, cb);
    }

    private <T> void sendWalletMessage(int type, Object r, IResponseCallback<T> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(type)
                .setData(r);
        send(b, cb);
    }

    @Override
    public void genSeed(WalletData.GenSeedRequest r, IResponseCallback<WalletData.GenSeedResponse> cb) {
        sendWalletMessage(AuthData.MESSAGE_TYPE_GEN_SEED, r, cb);
    }

    @Override
    public void initWallet(WalletData.InitWalletRequest r, IResponseCallback<WalletData.InitWalletResponse> cb) {
        sendWalletMessage(AuthData.MESSAGE_TYPE_INIT_WALLET, r, cb);
    }

    @Override
    public void unlockWallet(WalletData.UnlockWalletRequest r, IResponseCallback<WalletData.UnlockWalletResponse> cb) {
        sendWalletMessage(AuthData.MESSAGE_TYPE_UNLOCK_WALLET, r, cb);
    }

    @Override
    public void authorize(WalletData.AuthResponse res, IResponseCallback<Boolean> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(AuthData.MESSAGE_TYPE_AUTHED)
                .setAuthId(res.authId())
                .setUserId(res.authUserId())
                .setData(res);

        send(b, cb);
    }

    @Override
    public void getAuthRequest(int id, IResponseCallback<WalletData.AuthRequest> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(AuthData.MESSAGE_TYPE_GET)
                .setAuthId(id);

        send(b, cb);
    }

    @Override
    public <T> void getTransactionRequest(int userId, String txId, Class<T> cls, IResponseCallback<T> cb) {
        AuthData.AuthMessage.Builder b = AuthData.AuthMessage.builder()
                .setType(AuthData.MESSAGE_TYPE_GET_TX)
                .setUserId(userId)
                .setTxId(txId)
                .setData(cls);

        send(b, cb);
    }
}
