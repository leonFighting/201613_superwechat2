package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.bean.Member;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by leon on 2016/5/31.
 */
public class DownloadGroupMemberTask extends BaseActivity {
    private static final String TAG = DownloadGroupMemberTask.class.getName();
    Context mContext;
    String hxid;
    String path;

    public DownloadGroupMemberTask(Context mContext, String hxid) {
        this.mContext = mContext;
        this.hxid = hxid;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Member.GROUP_HX_ID,hxid)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Member[]>(path,Member[].class,
                responseDownloadGroupMemberTaskListener(),errorListener()));
    }

    private Response.Listener<Member[]> responseDownloadGroupMemberTaskListener() {
        return new Response.Listener<Member[]>() {
            @Override
            public void onResponse(Member[] members) {
                Log.e(TAG,"DownloadGroupMemberTask");
                if(members!=null){
                    Log.e(TAG,"DownloadGroupMemberTask,members size="+members.length);
                    HashMap<String, ArrayList<Member>> groupMembers = SuperWeChatApplication.getInstance().getGroupMembers();
                    ArrayList<Member> arrayList = Utils.array2List(members);
                    groupMembers.put(hxid,arrayList );
                    mContext.sendStickyBroadcast(new Intent("update_group_member"));
                }
            }
        };
    }

}
