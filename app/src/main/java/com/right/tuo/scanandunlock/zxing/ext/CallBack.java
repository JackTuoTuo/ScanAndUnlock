package com.right.tuo.scanandunlock.zxing.ext;

import com.google.zxing.Result;

/**
 * 扫码回调
 */
public interface CallBack {
    void success(Result result);
}
