/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ucai.superwechat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupInfo;
import com.easemob.chat.EMGroupManager;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.UserUtils;
import cn.ucai.superwechat.utils.Utils;

import com.easemob.exceptions.EaseMobException;

public class GroupSimpleDetailActivity extends BaseActivity {
	public static final String TAG = GroupSimpleDetailActivity.class.getName();
	private Button btn_add_group;
	private TextView tv_admin;
	private TextView tv_name;
	private TextView tv_introduction;
	private Group group;
	private String groupid;
	private ProgressBar progressBar;
	Activity mContext;
	NetworkImageView mivAvatar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_simle_details);
		mContext = this;
		initView();

		Group groupInfo = (Group) getIntent().getSerializableExtra("groupinfo");
		Log.e(TAG, "groupInfo=" + groupInfo);
		String groupname = null;
		if(groupInfo != null){
		    groupname = groupInfo.getMGroupName();
		    groupid = groupInfo.getMGroupHxid();
		}else{
		    group = PublicGroupsSeachActivity.searchedGroup;
		    if(group == null)
		        return;
		    groupname = group.getMGroupName();
		    groupid = group.getMGroupHxid();
		}
		
		tv_name.setText(groupname);
		
		
		if(group != null){
		    showGroupDetail();
		    return;
		}
//		new Thread(new Runnable() {
//
//			public void run() {
//				//从服务器获取详情
//				try {
//					group = EMGroupManager.getInstance().getGroupFromServer(groupid);
//					runOnUiThread(new Runnable() {
//						public void run() {
//							showGroupDetail();
//						}
//					});
//				} catch (final EaseMobException e) {
//					e.printStackTrace();
//					final String st1 = getResources().getString(R.string.Failed_to_get_group_chat_information);
//					runOnUiThread(new Runnable() {
//						public void run() {
//							progressBar.setVisibility(View.INVISIBLE);
//							Toast.makeText(GroupSimpleDetailActivity.this, st1+e.getMessage(), Toast.LENGTH_LONG).show();
//						}
//					});
//				}
//
//			}
//		}).start();
		try {
			String path = new ApiParams()
					.with(I.Group.HX_ID, groupid)
					.getRequestUrl(I.REQUEST_FIND_GROUP_BY_HXID);
			executeRequest(new GsonRequest<Group>(path,Group.class,
					responseListener(),errorListener()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		tv_name = (TextView) findViewById(R.id.name);
		tv_admin = (TextView) findViewById(R.id.tv_admin);
		btn_add_group = (Button) findViewById(R.id.btn_add_to_group);
		tv_introduction = (TextView) findViewById(R.id.tv_introduction);
		progressBar = (ProgressBar) findViewById(R.id.loading);
		mivAvatar = (NetworkImageView) findViewById(R.id.avatar);
	}

	private Response.Listener<Group> responseListener() {
		return new Response.Listener<Group>() {
			@Override
			public void onResponse(Group group) {
				if (group != null) {
					progressBar.setVisibility(View.INVISIBLE);
					//获取详情成功，并且自己不在群中，才让加入群聊按钮可点击
					if(!SuperWeChatApplication.getInstance().getGroupList().contains(group))
						btn_add_group.setEnabled(true);
					tv_name.setText(group.getMGroupName());
					tv_admin.setText(group.getMGroupOwner());
					tv_introduction.setText(group.getMGroupDescription());
					UserUtils.setGroupBeanAvatar(group.getMGroupHxid(),mivAvatar);
				} else {
					progressBar.setVisibility(View.INVISIBLE);
					final String st1 = getResources().getString(R.string.Failed_to_get_group_chat_information);
					Utils.showToast(mContext,st1,Toast.LENGTH_LONG);
				}
			}
		};
	}

	//加入群聊
	public void addToGroup(View view){
		String st1 = getResources().getString(R.string.Is_sending_a_request);
		final String st2 = getResources().getString(R.string.Request_to_join);
		final String st3 = getResources().getString(R.string.send_the_request_is);
		final String st4 = getResources().getString(R.string.Join_the_group_chat);
		final String st5 = getResources().getString(R.string.Failed_to_join_the_group_chat);
		final ProgressDialog pd = new ProgressDialog(this);
//		getResources().getString(R.string)
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					//如果是membersOnly的群，需要申请加入，不能直接join
					if(group.getMGroupAllowInvites()){
					    EMGroupManager.getInstance().applyJoinToGroup(groupid, st2);
					}else{
					    EMGroupManager.getInstance().joinGroup(groupid);
					}
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							if(group.getMGroupAllowInvites())
								Toast.makeText(GroupSimpleDetailActivity.this, st3, Toast.LENGTH_SHORT).show();
							else
								Toast.makeText(GroupSimpleDetailActivity.this, st4, Toast.LENGTH_SHORT).show();
							btn_add_group.setEnabled(false);
						}
					});
				} catch (final EaseMobException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(GroupSimpleDetailActivity.this, st5+e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}
	
     private void showGroupDetail() {
         progressBar.setVisibility(View.INVISIBLE);
         //获取详情成功，并且自己不在群中，才让加入群聊按钮可点击
         if(!SuperWeChatApplication.getInstance().getGroupList().contains(group))
             btn_add_group.setEnabled(true);
         tv_name.setText(group.getMGroupName());
         tv_admin.setText(group.getMGroupOwner());
         tv_introduction.setText(group.getMGroupDescription());
		 UserUtils.setGroupBeanAvatar(group.getMGroupHxid(),mivAvatar);
     }
	
	public void back(View view){
		finish();
	}
}
