package client.command;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import net.server.Channel;
import net.server.Server;
import scripting.npc.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import client.IItem;
import client.ISkill;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleStat;
import client.SkillFactory;

public class GMCommands extends Commands {
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		Server serv = Server.getInstance();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
				return false;
			case ap:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.setRemainingAp(Integer.parseInt(sub[2]));
					victim.updateSingleStat(MapleStat.AVAILABLEAP, Integer.parseInt(sub[2]));
				} else if (sub.length == 2) {
					chr.setRemainingAp(Integer.parseInt(sub[1]));
					chr.updateSingleStat(MapleStat.AVAILABLEAP, Integer.parseInt(sub[1]));
				} else {
					chr.message("Usage: !ap number || !ap playerName number");
				}
				break;
			case buff:
				if (sub.length == 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
					for (int i : array) {
						SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(victim);
					}
				} else if (sub.length == 1) {
					final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
					for (int i : array) {
						SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(chr);
					}
				} else {
					chr.message("Usage: !buff || !buff playerName");
				}
				break;
			case dc:
				if (sub.length == 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
				} else {
					chr.message("Usage: !dc playerName");
				}
				break;
			case dispose:
				if (sub.length == 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					NPCScriptManager.getInstance().dispose(victim.getClient());
					victim.getClient().announce(MaplePacketCreator.enableActions());
					chr.message("Done.");
				} else {
					chr.message("Usage: !dispose playerName");
				}
				break;
			case drop:
				int itemId = Integer.parseInt(sub[1]);
				short quantity = 1;
				try {
					quantity = Short.parseShort(sub[2]);
				} catch (Exception e) {
				}
				IItem toDrop;
				if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
					toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
				} else {
					toDrop = new Item(itemId, (byte) 0, quantity);
				}
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
				break;
			case fame:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.setFame(Integer.parseInt(sub[2]));
					victim.updateSingleStat(MapleStat.FAME, Integer.parseInt(sub[2]));
				} else if (sub.length == 2) {
					chr.setFame(Integer.parseInt(sub[1]));
					chr.updateSingleStat(MapleStat.FAME, Integer.parseInt(sub[1]));
				} else {
					chr.message("Usage: !job number || !job playerName number");
				}
				break;
			case gmshop:
				MapleShopFactory.getInstance().getShop(1337).sendShop(c);
				break;
			case heal:
				if (sub.length == 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.setHpMp(30000);
				} else if (sub.length == 1) {
					chr.setHpMp(30000);
				} else {
					chr.message("Usage: !heal || !heal playerName");
				}
				break;
			case job:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.changeJob(MapleJob.getById(Integer.parseInt(sub[2])));
					victim.equipChanged();
				} else if (sub.length == 2) {
					chr.changeJob(MapleJob.getById(Integer.parseInt(sub[1])));
					chr.equipChanged();
				} else {
					chr.message("Usage: !job number || !job playerName number");
				}
				break;
			case kill:
				if (sub.length == 2) {
					cserv.getPlayerStorage().getCharacterByName(sub[1]).setHpMp(0);
				} else {
					chr.message("Usage: !kill playerName");
				}
				break;
			case killall:
				List<MapleMapObject> monsters = chr.getMap().getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
				MapleMap map = chr.getMap();
				for (MapleMapObject monstermo : monsters) {
					MapleMonster monster = (MapleMonster) monstermo;
					map.killMonster(monster, chr, true);
					monster.giveExpToCharacter(chr, monster.getExp() * c.getPlayer().getExpRate(), true, 1);
				}
				chr.message("Killed " + monsters.size() + " monsters.");
				break;
			case level:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					chr.setLevel(Integer.parseInt(sub[2]));
					chr.setExp(0);
					chr.updateSingleStat(MapleStat.LEVEL, Integer.parseInt(sub[2]));
					chr.updateSingleStat(MapleStat.EXP, 0);
				} else if (sub.length == 2) {
					chr.setLevel(Integer.parseInt(sub[1]));
					chr.setExp(0);
					chr.updateSingleStat(MapleStat.LEVEL, Integer.parseInt(sub[1]));
					chr.updateSingleStat(MapleStat.EXP, 0);
				} else {
					chr.message("Usage: !level number || !level playerName number");
				}
				break;
			case levelup:
				if (sub.length == 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.levelUp(true);
				} else if (sub.length == 1) {
					chr.levelUp(true);
				} else {
					chr.message("Usage: !levelup || !levelup playerName");
				}
				break;
			case map:
				if (sub.length >= 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.changeMap(Integer.parseInt(sub[2]));
				} else if (sub.length == 2) {
					chr.changeMap(Integer.parseInt(sub[1]));
				} else {
					chr.message("Usage: !map number || !map playerName number");
				}
				break;
			case maxskills:
				if (sub.length == 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]); 
					for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
		                try {
		                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
		                    victim.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
		                } catch (NumberFormatException nfe) {
		                    break;
		                } catch (NullPointerException npe) {
		                    continue;
		                }
		            }
				} else if (sub.length == 1) {
					for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
		                try {
		                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
		                    chr.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
		                } catch (NumberFormatException nfe) {
		                    break;
		                } catch (NullPointerException npe) {
		                    continue;
		                }
		            }
				} else {
					chr.message("Usage: !maxskills || !maxskills playerName");
				}
				break;
			case maxstats:
				if (sub.length == 2) {
					final String[] s = {"setall", sub[1], String.valueOf(Short.MAX_VALUE)};
					execute(c, s, heading);
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.setLevel(255);
					victim.setFame(Short.MAX_VALUE);
					victim.setMaxHp(30000);
					victim.setMaxMp(30000);
					victim.updateSingleStat(MapleStat.LEVEL, 255);
					victim.updateSingleStat(MapleStat.FAME, Short.MAX_VALUE);
					victim.updateSingleStat(MapleStat.MAXHP, 30000);
					victim.updateSingleStat(MapleStat.MAXMP, 30000);
				} else if (sub.length == 1) {
					final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
					execute(c, s, heading);
					chr.setLevel(255);
					chr.setFame(Short.MAX_VALUE);
					chr.setMaxHp(30000);
					chr.setMaxMp(30000);
					chr.updateSingleStat(MapleStat.LEVEL, 255);
					chr.updateSingleStat(MapleStat.FAME, Short.MAX_VALUE);
					chr.updateSingleStat(MapleStat.MAXHP, 30000);
					chr.updateSingleStat(MapleStat.MAXMP, 30000);
				} else {
					chr.message("Usage: !maxstats || !maxstats playerName");
				}
				break;
			case mesos:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.gainMeso(Integer.parseInt(sub[2]), true);
				} else if (sub.length == 2) {
					chr.gainMeso(Integer.parseInt(sub[1]), true);
				} else {
					chr.message("Usage: !mesos number || !mesos playerName number");
				}
				break;
			case pap:
				chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), chr.getPosition());
				break;
			case pianus:
				chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), chr.getPosition());
				break;
			case notice:
				Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(6, "[Notice] " + joinStringFrom(sub, 1)));
				break;
			case saveall:
				for (Channel chan : Server.getInstance().getAllChannels()) {
	                for (MapleCharacter plyrs : chan.getPlayerStorage().getAllCharacters()) {
	                    plyrs.saveToDB(true);
	                }
	            }
	            chr.message("Save Complete.");
				break;
			case servermessage:
				c.getWorldServer().setServerMessage(joinStringFrom(sub, 1));
				break;
			case setall:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					final int x = Short.parseShort(sub[2]);
					victim.setStr(x);
					victim.setDex(x);
					victim.setInt(x);
					victim.setLuk(x);
					victim.updateSingleStat(MapleStat.STR, x);
					victim.updateSingleStat(MapleStat.DEX, x);
					victim.updateSingleStat(MapleStat.INT, x);
					victim.updateSingleStat(MapleStat.LUK, x);
				} else if (sub.length == 2) {
					final int x = Short.parseShort(sub[1]);
					chr.setStr(x);
					chr.setDex(x);
					chr.setInt(x);
					chr.setLuk(x);
					chr.updateSingleStat(MapleStat.STR, x);
					chr.updateSingleStat(MapleStat.DEX, x);
					chr.updateSingleStat(MapleStat.INT, x);
					chr.updateSingleStat(MapleStat.LUK, x);
				} else {
					chr.message("Usage: !setall number || !setall playerName number");
				}
				break;
			case shop:
				if (sub.length == 2) {
					MapleShopFactory.getInstance().getShop(Integer.parseInt(sub[1])).sendShop(c);
				} else {
					chr.message("Usage: !shop number");
				}
				break;
			case sp:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.setRemainingSp(Integer.parseInt(sub[2]));
					victim.updateSingleStat(MapleStat.AVAILABLESP, Integer.parseInt(sub[2]));
				} else if (sub.length == 2) {
					chr.setRemainingSp(Integer.parseInt(sub[1]));
					chr.updateSingleStat(MapleStat.AVAILABLESP, Integer.parseInt(sub[1]));
				} else {
					chr.message("Usage: !ap number || !ap playerName number");
				}
				break;
			case spawn:
				if (sub.length > 2) {
					for (int i = 0; i < Integer.parseInt(sub[2]); i++) {
						chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(sub[1])), chr.getPosition());
					}
				} else {
					chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(sub[1])), chr.getPosition());
				}
				break;
			case speak:
				if (sub.length == 2) {
					NPCScriptManager.getInstance().start(c, Integer.parseInt(sub[1]), null, null);
				} else {
					chr.message("Usage: !speak number");
				}
				break;
			case texttype:
				chr.toggleGMText();
				chr.message("Your chat messages are now " + (chr.getGMText() ? "white." : "black."));
				break;
			case unban:
				try {
					PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getIdByName(sub[1]));
					p.executeUpdate();
					p.close();
				} catch (Exception e) {
					chr.message("Failed to unban " + sub[1]);
				}
				chr.message("Unbanned " + sub[1]);
				break;
		}
		return true;
	}
	
	private static enum Command {
		ap,
		buff,
		dc,
		dispose,
		drop,
		fame,
		gmshop,
		heal,
		job,
		kill,
		killall,
		level,
		levelup,
		map,
		maxskills,
		maxstats,
		mesos,
		pap,
		pianus,
		notice,
		saveall,
		search,
		servermessage,
		setall,
		shop,
		sp,
		spawn,
		speak,
		texttype,
		unban,
	}
}