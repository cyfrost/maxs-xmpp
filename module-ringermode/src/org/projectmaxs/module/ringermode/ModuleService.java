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

package org.projectmaxs.module.ringermode;

import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.module.ringermode.commands.RingermodeNormal;
import org.projectmaxs.module.ringermode.commands.RingermodeShow;
import org.projectmaxs.module.ringermode.commands.RingermodeSilent;
import org.projectmaxs.module.ringermode.commands.RingermodeVibrate;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-ringermode", sCOMMANDS);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.ringermode",      // Package of the Module
			"MAXS Module Ringermode"                  // Name of the Module
			);
	// @formatter:on

	public static final SupraCommand RINGERMODE = new SupraCommand("ringermode", "ringer");

	public static final SupraCommand[] sCOMMANDS;

	static {
		Set<SupraCommand> commands = new HashSet<SupraCommand>();

		SupraCommand.register(RingermodeShow.class, commands);
		SupraCommand.register(RingermodeNormal.class, commands);
		SupraCommand.register(RingermodeSilent.class, commands);
		SupraCommand.register(RingermodeVibrate.class, commands);

		sCOMMANDS = commands.toArray(new SupraCommand[commands.size()]);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

}
