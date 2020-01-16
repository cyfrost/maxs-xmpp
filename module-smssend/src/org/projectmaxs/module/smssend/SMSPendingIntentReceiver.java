/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.module.smssend;

import org.projectmaxs.module.smssend.commands.AbstractSmsSendCommand;
import org.projectmaxs.module.smssend.database.SMSTable;
import org.projectmaxs.module.smssend.database.SMSTable.SMSInfo;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.module.ContactUtil;
import org.projectmaxs.shared.module.MainUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SMSPendingIntentReceiver extends BroadcastReceiver {

	public static final String SMS_SENT_ACTION = ModuleService.PACKAGE + ".SMS_SENT";
	public static final String SMS_DELIVERED_ACTION = ModuleService.PACKAGE + ".SMS_DELIVERED";

	private static final Log LOG = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		Message message = null;
		String action = intent.getAction();
		int partNum = intent.getIntExtra(AbstractSmsSendCommand.PART_NUM_EXTRA, -1);
		int cmdId = intent.getIntExtra(AbstractSmsSendCommand.CMD_ID_EXTRA, -1);
		int res = getResultCode();
		LOG.d("onReceive: action=" + action + " partNum=" + partNum + " cmdId=" + cmdId + " res="
				+ res);
		SMSTable smsTable = SMSTable.getInstance(context);
		SMSInfo smsInfo = smsTable.getSMSInfo(cmdId);
		Contact contact = ContactUtil.getInstance(context).contactByNumber(smsInfo.mReceiver);
		if (SMS_SENT_ACTION.equals(action)) {
			String sentIntents = smsTable.getIntents(cmdId, SMSTable.IntentType.SENT);
			sentIntents = markPart(sentIntents, partNum, smsResultToChar(res));
			smsTable.updateIntents(cmdId, sentIntents, SMSTable.IntentType.SENT);
			if (allMarkedNoError(sentIntents)) {
				message = new Message("SMS sent to "
						+ ContactUtil.prettyPrint(smsInfo.mReceiver, contact) + ": "
						+ smsInfo.mShortText);
			}
			// TODO Add mechanism to display sent failure reasons
		} else if (SMS_DELIVERED_ACTION.equals(action)) {
			String deliveredIntents = smsTable.getIntents(cmdId, SMSTable.IntentType.DELIVERED);
			deliveredIntents = markPart(deliveredIntents, partNum, RESULT_NO_ERROR_CHAR);
			smsTable.updateIntents(cmdId, deliveredIntents, SMSTable.IntentType.DELIVERED);
			if (allMarkedNoError(deliveredIntents)) {
				message = new Message("SMS delivered to "
						+ ContactUtil.prettyPrint(smsInfo.mReceiver, contact) + ": "
						+ smsInfo.mShortText);
			}
		} else {
			throw new IllegalStateException("Unknown action=" + action
					+ " in SMSPendingIntentReceiver");
		}
		if (message != null) MainUtil.send(message, context);
	}

	private static String markPart(String intents, int partNum, char mark) {
		char[] intentsChars = intents.toCharArray();
		intentsChars[partNum] = mark;
		return new String(intentsChars);
	}

	private static boolean allMarkedNoError(String intents) {
		char[] intentsChars = intents.toCharArray();
		for (int i = 0; i < intentsChars.length; i++)
			if (intentsChars[i] != RESULT_NO_ERROR_CHAR) return false;

		return true;
	}

	private static final char RESULT_NO_ERROR_CHAR = 'X';
	private static final char RESULT_ERROR_GENERIC_FAILURE_CHAR = 'G';
	private static final char RESULT_ERROR_NO_SERVICE_CHAR = 'S';
	private static final char RESULT_ERROR_NULL_PDU_CHAR = 'P';
	private static final char RESULT_ERROR_RADIO_OFF_CHAR = 'R';

	private static char smsResultToChar(int res) {
		switch (res) {
		case Activity.RESULT_OK:
			return RESULT_NO_ERROR_CHAR;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			return RESULT_ERROR_GENERIC_FAILURE_CHAR;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			return RESULT_ERROR_NO_SERVICE_CHAR;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			return RESULT_ERROR_NULL_PDU_CHAR;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			return RESULT_ERROR_RADIO_OFF_CHAR;
		default:
			throw new IllegalStateException("unknown res=" + res);
		}
	}
}
