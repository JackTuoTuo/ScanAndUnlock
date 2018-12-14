package com.right.tuo.scanandunlock.bean;

/**
 * 描述
 *
 * @author xuhj
 */
public class BaseResponse<T> {
    private int codeId;
    private String codeDes;
    private T resData;

    public int getCodeId() {
        return codeId;
    }

    public void setCodeId(int codeId) {
        this.codeId = codeId;
    }

    public String getCodeDes() {
        return codeDes;
    }

    public void setCodeDes(String codeDes) {
        this.codeDes = codeDes;
    }

    public T getResData() {
        return resData;
    }

    public void setResData(T resData) {
        this.resData = resData;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "codeId='" + codeId + '\'' +
                ", codeDes='" + codeDes + '\'' +
                ", resData=" + resData +
                '}';
    }
}
