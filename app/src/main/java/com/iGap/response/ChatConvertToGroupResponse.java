/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package com.iGap.response;

import com.iGap.G;
import com.iGap.module.enums.GroupChatRole;
import com.iGap.module.enums.RoomType;
import com.iGap.proto.ProtoChatConvertToGroup;
import com.iGap.proto.ProtoError;
import com.iGap.proto.ProtoGlobal;
import com.iGap.realm.RealmGroupRoom;
import com.iGap.realm.RealmRoom;
import com.iGap.realm.RealmRoomFields;
import io.realm.Realm;

public class ChatConvertToGroupResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public ChatConvertToGroupResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);
        this.message = protoClass;
        this.identity = identity;
        this.actionId = actionId;
    }

    @Override
    public void handler() {
        super.handler();
        final ProtoChatConvertToGroup.ChatConvertToGroupResponse.Builder builder = (ProtoChatConvertToGroup.ChatConvertToGroupResponse.Builder) message;


        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmRoom realmRoom = realm.where(RealmRoom.class).equalTo(RealmRoomFields.ID, builder.getRoomId()).findFirst();
                realmRoom.setType(RoomType.GROUP);
                realmRoom.setTitle(builder.getName());
                RealmGroupRoom realmGroupRoom = realm.createObject(RealmGroupRoom.class);
                if (builder.getRole() == ProtoGlobal.GroupRoom.Role.OWNER) {
                    realmGroupRoom.setRole(GroupChatRole.OWNER);
                } else {
                    realmGroupRoom.setRole(GroupChatRole.MEMBER);

                }
                realmGroupRoom.setDescription(builder.getDescription());
                realmGroupRoom.setParticipantsCountLabel("2");
                realmRoom.setGroupRoom(realmGroupRoom);
                realmRoom.setChatRoom(null);
            }
        });
        realm.close();


        G.onChatConvertToGroup.onChatConvertToGroup(builder.getRoomId(), builder.getName(), builder.getDescription(), builder.getRole());
    }

    @Override
    public void timeOut() {
        super.timeOut();
        G.onChatConvertToGroup.timeOut();
    }

    @Override
    public void error() {
        super.error();

        ProtoError.ErrorResponse.Builder errorResponse = (ProtoError.ErrorResponse.Builder) message;
        int majorCode = errorResponse.getMajorCode();
        int minorCode = errorResponse.getMinorCode();

        G.onChatConvertToGroup.Error(majorCode, minorCode);
    }
}


