package com.cl.common_base.widget.edittext.bean;

import com.cl.common_base.widget.edittext.listener.InsertData;

import java.io.Serializable;
import java.util.Objects;

/**
 * 艾特用户
 */
public class MentionUser implements Serializable, InsertData {

    private final CharSequence userId;
    private final CharSequence userName;

    private final CharSequence abbyId;

    private final CharSequence nickName;

    private final CharSequence picture;

    /**
     *   val abbyId: String? = null,
     *     val nickName: String? = null,
     *     val picture: String? = null,
     *     val userId: String? = null,
     *     var isSelect: Boolean? = false
     * @param userId
     * @param userName
     */
    public MentionUser(CharSequence userId, CharSequence userName, CharSequence abbyId, CharSequence nickName, CharSequence picture) {
        this.userId = userId;
        this.userName = userName;
        this.abbyId = abbyId;
        this.nickName = nickName;
        this.picture = picture;
    }

    public CharSequence getUserId() {
        return userId;
    }

    public CharSequence getUserName() {
        return userName;
    }

    public CharSequence getPicture() {
        return picture;
    }

    public CharSequence getAbbyId() {
        return abbyId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MentionUser user = (MentionUser) o;

        if (!Objects.equals(userId, user.userId)) return false;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }

    @Override
    public CharSequence charSequence() {
        return "@" + userName + " ";
    }

    @Override
    public FormatRange.FormatData formatData() {
        return new UserConvert(this);
    }

    @Override
    public int color() {
        return 0xFF008961;
    }

    private class UserConvert implements FormatRange.FormatData {

        private final MentionUser user;

        public UserConvert(MentionUser user) {
            this.user = user;
        }

        @Override
        public FormatItemResult formatResult() {
            FormatItemResult userResult = new FormatItemResult();
            userResult.setId(user.getUserId().toString());
            userResult.setName(user.getUserName().toString());
            userResult.setPicture(user.getPicture().toString());
            userResult.setAbbyId(user.getAbbyId().toString());
            return userResult;
        }
    }
}