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

package org.projectmaxs.module.smsnotify;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleReceiver;

import android.content.Context;
import android.content.SharedPreferences;

public class ModuleReceiver extends MAXSModuleReceiver {
	private final static Log LOG = Log.getLog();

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.smsnotify",
			"MAXS Module SmsNotify"
	);
	// @formatter:on

	public ModuleReceiver() {
		super(LOG, sMODULE_INFORMATION);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	@Override
	public SharedPreferences getSharedPreferences(Context context) {
		return Settings.getInstance(context).getSharedPreferences();
	}
}
