package br.com.mtanuri.liferay.notification;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.blogs.constants.BlogsPortletKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.notifications.BaseModelUserNotificationHandler;
import com.liferay.portal.kernel.notifications.UserNotificationDefinition;
import com.liferay.portal.kernel.notifications.UserNotificationHandler;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author marceltanuri
 */

@Component(immediate = true, property = { "javax.portlet.name=" + BlogsPortletKeys.BLOGS,
		"service.ranking:Integer=100" }, service = UserNotificationHandler.class)

public class CustomBlogsUserNotificationHandler extends BaseModelUserNotificationHandler {

	private static final String EMPTY_STRING = "";

	private static final String BLOG_ENTRY_CLASS_NAME = "com.liferay.blogs.model.BlogsEntry";

	@Reference
	private AssetEntryLocalService assetEntryLocalService;

	@Reference
	private GroupLocalService groupLocalService;

	@Reference
	private LayoutLocalService layoutLocalService;

	public CustomBlogsUserNotificationHandler() {
		setPortletId(BlogsPortletKeys.BLOGS);
	}

	@Override
	protected String getTitle(JSONObject jsonObject, AssetRenderer<?> assetRenderer, ServiceContext serviceContext) {

		String message = EMPTY_STRING;

		AssetRendererFactory<?> assetRendererFactory = AssetRendererFactoryRegistryUtil
				.getAssetRendererFactoryByClassName(assetRenderer.getClassName());

		String typeName = assetRendererFactory.getTypeName(serviceContext.getLocale());

		int notificationType = jsonObject.getInt("notificationType");

		if (notificationType == UserNotificationDefinition.NOTIFICATION_TYPE_ADD_ENTRY) {

			if (jsonObject.getString("className") != null
					&& jsonObject.getString("className").equals(BLOG_ENTRY_CLASS_NAME)) {
				String categoria = EMPTY_STRING;
				try {
					AssetEntry entry = assetEntryLocalService.getEntry(jsonObject.getString("className"),
							jsonObject.getInt("classPK"));
					Group group = groupLocalService.getGroup(entry.getGroupId());
					if (group.getParentGroupId() != 0) {
						Layout layout = layoutLocalService.getLayout(group.getClassPK());
						categoria = layout.getName("en_US");
						_log.debug("Group name: " + categoria);
					}
				} catch (PortalException e) {
					e.printStackTrace();
				}
				String userName = HtmlUtil.escape(_getUserFullName(jsonObject));
				message = userName + " adicionou um novo post";
				if (!categoria.isEmpty()) {
					message = message + " em " + categoria;
				}
				return message + ".";
			} else {
				message = "x-added-a-new-x";
			}

		} else if (notificationType == UserNotificationDefinition.NOTIFICATION_TYPE_UPDATE_ENTRY) {

			message = "x-updated-a-x";
		}

		return getFormattedMessage(jsonObject, serviceContext, message, typeName);
	}

	private String _getUserFullName(JSONObject jsonObject) {
		String fullName = jsonObject.getString("fullName");

		if (Validator.isNotNull(fullName)) {
			return fullName;
		}

		return PortalUtil.getUserName(jsonObject.getLong("userId"), EMPTY_STRING);
	}

	private static final Log _log = LogFactoryUtil.getLog(CustomBlogsUserNotificationHandler.class);

}