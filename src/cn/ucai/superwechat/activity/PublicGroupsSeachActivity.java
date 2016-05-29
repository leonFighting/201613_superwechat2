package cn.ucai.superwechat.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.UserUtils;
import cn.ucai.superwechat.utils.Utils;


public class PublicGroupsSeachActivity extends BaseActivity {
    public static final String TAG = PublicGroupsSeachActivity.class.getName();
    private RelativeLayout containerLayout;
    private EditText idET;
    private TextView nameText;
    public static Group searchedGroup;
    NetworkImageView mivAvatar;
    ProgressDialog pd;
    Context mContext;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_public_groups_search);
        mContext = this;
        intiView();
        searchedGroup = null;
    }

    private void intiView() {
        containerLayout = (RelativeLayout) findViewById(R.id.rl_searched_group);
        idET = (EditText) findViewById(R.id.et_search_id);
        nameText = (TextView) findViewById(R.id.name);
        mivAvatar = (NetworkImageView) findViewById(R.id.avatar);
    }

    /**
     * 搜索
     *
     * @param v
     */
    public void searchGroup(View v) {
        if (TextUtils.isEmpty(idET.getText())) {
            return;
        }

        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.searching));
        pd.setCancelable(false);
        pd.show();
        /* url=http://10.0.2.2:8080/SuperWeChatServer/Server?
        request=find_group_by_group_name&m_group_name=*/
        String hxid = idET.getText().toString();
        Log.e(TAG, "searchGroup,hxid=" + hxid);
        try {
            String path = new ApiParams()
                    .with(I.Group.HX_ID, hxid)
                    .getRequestUrl(I.REQUEST_FIND_GROUP_BY_HXID);
            Log.e(TAG, "searchGroup,path=" + path);
            executeRequest(new GsonRequest<Group>(path,Group.class,
                    responseListener(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Response.Listener<Group> responseListener() {
        return new Response.Listener<Group>() {
            @Override
            public void onResponse(Group group) {
                Log.e(TAG, "searchGroup,responseListener,group=" + group.toString());
                if (group != null) {
                    pd.dismiss();
                    searchedGroup = group;
                    String mGroupName = searchedGroup.getMGroupName();
                    containerLayout.setVisibility(View.VISIBLE);
                    nameText.setText(mGroupName);
                    UserUtils.setGroupBeanAvatar(group.getMGroupHxid(), mivAvatar);
                } else {
                    pd.dismiss();
                    searchedGroup = null;
                    containerLayout.setVisibility(View.GONE);
                    Utils.showToast(mContext,Utils.getResourceString(mContext,R.string.group_not_existed),Toast.LENGTH_SHORT);
                }
            }
        };
    }


    /**
     * 点击搜索到的群组进入群组信息页面
     *
     * @param view
     */
    public void enterToDetails(View view) {
        startActivity(new Intent(this, GroupSimpleDetailActivity.class));
    }
}
