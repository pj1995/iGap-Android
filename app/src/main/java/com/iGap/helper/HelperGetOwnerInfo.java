/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package com.iGap.helper;

import com.iGap.G;
import com.iGap.interfaces.OnClientGetRoomResponse;
import com.iGap.interfaces.OnUserInfoResponse;
import com.iGap.proto.ProtoClientGetRoom;
import com.iGap.proto.ProtoGlobal;
import com.iGap.realm.RealmRegisteredInfo;
import com.iGap.realm.RealmRegisteredInfoFields;
import com.iGap.realm.RealmRoom;
import com.iGap.realm.RealmRoomFields;
import com.iGap.request.RequestClientGetRoom;
import com.iGap.request.RequestUserInfo;
import io.realm.Realm;


public class HelperGetOwnerInfo {

    public interface Listener {

        void OnResponse();
    }

    enum RoomType {
        Room,
        User;
    }

    public static void checkInfo(long id, RoomType roomType, Listener listener) {

        switch (roomType) {

            case Room:
                checkRoomExist(id, listener);
                break;
            case User:
                checkUserExist(id, listener);
                break;
        }
    }

    private static void checkRoomExist(long id, final Listener listener) {

        Realm realm = Realm.getDefaultInstance();

        RealmRoom realmRoom = realm.where(RealmRoom.class).equalTo(RealmRoomFields.ID, id).findFirst();

        if (realmRoom == null) {

            G.onClientGetRoomResponse = new OnClientGetRoomResponse() {
                @Override public void onClientGetRoomResponse(ProtoGlobal.Room room, ProtoClientGetRoom.ClientGetRoomResponse.Builder builder, String identity) {

                    if (identity.equals(RequestClientGetRoom.CreateRoomMode.requestFromOwner.toString())) {

                        if (listener != null) {
                            listener.OnResponse();
                        }
                    }
                }

                @Override public void onError(int majorCode, int minorCode) {

                }

                @Override public void onTimeOut() {

                }
            };

            new RequestClientGetRoom().clientGetRoom(id, RequestClientGetRoom.CreateRoomMode.requestFromOwner);
        } else {

            if (listener != null) {
                listener.OnResponse();
            }
        }

        realm.close();
    }

    private static void checkUserExist(long userId, final Listener listener) {

        Realm realm = Realm.getDefaultInstance();

        RealmRegisteredInfo registeredInfo = realm.where(RealmRegisteredInfo.class).equalTo(RealmRegisteredInfoFields.ID, userId).findFirst();

        if (registeredInfo == null) {

            G.onUserInfoResponse = new OnUserInfoResponse() {
                @Override public void onUserInfo(ProtoGlobal.RegisteredUser user, String identity) {

                    if (listener != null) {
                        listener.OnResponse();
                    }
                }

                @Override public void onUserInfoTimeOut() {

                }

                @Override public void onUserInfoError(int majorCode, int minorCode) {

                }
            };

            new RequestUserInfo().userInfo(userId);
        } else {

            if (listener != null) {
                listener.OnResponse();
            }
        }

        realm.close();
    }
}
