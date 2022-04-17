package com.weikun.api.common;

import com.baidu.aip.face.AipFace;

/**
 * 单例加载百度SDK
 * 创建人：SHI
 * 创建时间：2021/11/22
 *
 */
public class AIFactoryUtil {
	private volatile static AipFace aipFace;

	public static AipFace getAipFace(){
		if(aipFace==null){
			synchronized (AipFace.class) {
				if(aipFace==null){
					aipFace = new AipFace(AIConstant.BD_FACE_APPID, AIConstant.BD_FACE_APPKEY, AIConstant.BD_FACE_SECRETKEY);
				}
			}
		}
		return aipFace;
	}

}
