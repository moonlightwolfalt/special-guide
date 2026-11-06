package io.mopesbox.Utils;

import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Client.GameClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;

public class GUIThread extends Thread {
    private static Terminal terminal = null;
    private DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    private static TextGraphics textGraphics;
    // private static Main main;

    public GUIThread(Main main) {
        // GUIThread.main = main;
        if(Constants.GUIENABLED) {
            try {
                terminal = defaultTerminalFactory.createTerminal();
                terminal.enterPrivateMode();
                terminal.clearScreen();
                terminal.setCursorVisible(false);
                textGraphics = terminal.newTextGraphics();
                terminal.addResizeListener(GUIThread::onResize);
                GUIThread.draw();
            } catch (IOException e) {
                textGraphics = null;
                e.printStackTrace();
            }
        } else {
            System.out.println("GUI Disabled.");
        }
    }

    @Override
    public void run() {
        if(Constants.GUIENABLED) {
            while (true) {
                try {
                    this.update();
                    Thread.sleep(Main.instance.room.tick);
                //  Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int g_players = 0;
    private static List<Integer> g_staff = new ArrayList<>();

    public void changePlayers(int plrs, List<Integer> staff) {
        if(Constants.GUIENABLED) {
            g_players = plrs;
            g_staff = staff;
        }
    }

    public void toLog(String log) {
        if(Constants.GUIENABLED) {
            g_log.add(log);
        }
    }

    public void changeWebsocketStatus(boolean enabled) {
        if(Constants.GUIENABLED) {
            isWebsocketStarted = enabled;
        }
    }

    static public void onResize(Terminal terminal1, TerminalSize newSize) {
        if(Constants.GUIENABLED) {
            GUIThread.terminal = terminal1;
            draw();
        }
    }

    private static List<String> g_log = new ArrayList<>();

    public static boolean isWebsocketStarted = false;

    public static int choosedAction = 0;

    public static int choosedActionInTab = 0;

    public static int choosedActionInTab2 = 0;
    
    public static int choosedActionInPlayer = 0;

    public static boolean showMenu = false;

    public static boolean showFullStatistic = false;

    public static boolean inMenuTab = false;

    public static int choosedActionInMenu = 0;

    public static void draw() {
        try {
            terminal.clearScreen();
            textGraphics.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
            textGraphics.putString(0, 0,
                    "Game Mode: "
                            + (Constants.GAMEMODE == 0 ? "FFA"
                                    : Constants.GAMEMODE == 1 ? "SANDBOX"
                                            : Constants.GAMEMODE == 2 ? "BATTLE ROYALE"
                                                    : Constants.GAMEMODE == 3 ? "TEAMS"
                                                            : Constants.GAMEMODE == 4 ? "WILD MOPE"
                                                                    : Constants.GAMEMODE == 5 ? "ZOMBIE"
                                                                            : Constants.GAMEMODE == 6 ? "PANDEMIC" : Constants.GAMEMODE == 7 ? "JUGGERNAUT"
                                                                                    : "UNKNOWN")
                            + " (" + Constants.GAMEMODE + ") | " + "Websocket Status: " + (isWebsocketStarted ? "ONLINE" : "OFFLINE")
                            + " | v. " + Constants.VERSION + " " + (Constants.isExperimental ? "EXP" : Constants.isBeta ? "BETA" : "LIVE"),
                    SGR.BOLD);
            textGraphics.putString(0, 1, "Connected: " + g_players + " players", SGR.BOLD);
            if(!showFullStatistic && !showMenu) {
                // actions
                textGraphics.setForegroundColor(TextColor.ANSI.MAGENTA_BRIGHT);
                textGraphics.putString(0, 2, "Action:", SGR.BOLD);
                String menuString = "MENU";
                if(choosedAction == 0) {
                    textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                    textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                    menuString = "> MENU <";
                } else {
                    textGraphics.setForegroundColor(TextColor.ANSI.MAGENTA_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                }
                textGraphics.putString("Action:".length()+1, 2, menuString, SGR.BOLD);
                String staticticString = "STATISTIC";
                if(choosedAction == 1) {
                    textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                    textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                    staticticString = "> STATISTIC <";
                } else {
                    textGraphics.setForegroundColor(TextColor.ANSI.MAGENTA_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                }
                textGraphics.putString(("Action: "+menuString).length()+1, 2, staticticString, SGR.BOLD);
                textGraphics.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
                textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                // end actions
                if (g_staff.size() > 0) {
                    int offset = 0;
                    String str1 = "Staff Online: " + (g_staff.get(0) + g_staff.get(1) + g_staff.get(2));
                    textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                    textGraphics.putString(terminal.getTerminalSize().getColumns() - str1.length(), offset, str1, SGR.BOLD);
                    offset++;
                    List<String> strs = new ArrayList<>();
                    if (g_staff.get(0) > 0) {
                        strs.add("Artists: " + g_staff.get(0));
                    }
                    if (g_staff.get(1) > 0) {
                        strs.add("Admins: " + g_staff.get(1));
                    }
                    if (g_staff.get(2) > 0) {
                        strs.add("Developers: " + g_staff.get(2));
                    }
                    String str = String.join(" | ", strs);
                    textGraphics.putString(terminal.getTerminalSize().getColumns() - str.length(), offset, str,
                                SGR.BOLD);
                }
                textGraphics.setForegroundColor(TextColor.ANSI.YELLOW_BRIGHT);
                textGraphics.drawLine(0, 3, (terminal.getTerminalSize().getColumns() - 1), 3, '–');
                String chatstr = "LOG";
                textGraphics.putString((terminal.getTerminalSize().getColumns() / 2) - (chatstr.length() / 2), 3, chatstr,
                        SGR.BOLD);
                if (g_log.size() > 0 && terminal.getTerminalSize().getRows() > 4) {
                    int offset = 4;
                    List<String> newarr = g_log
                            .subList(Math.max(0, g_log.size() - (terminal.getTerminalSize().getRows() - 5)), g_log.size());
                    for (String line : newarr) {
                        line = line.trim();
                        if (line.length() > (terminal.getTerminalSize().getColumns() - 1) / 2) {
                            List<String> lines = new ArrayList<>();
                            Pattern pattern = Pattern
                                    .compile(".{1," + ((terminal.getTerminalSize().getColumns() - 1) / 2) + "}");
                            Matcher matcher = pattern.matcher(line);
                            while (matcher.find()) {
                                lines.add(line.substring(matcher.start(), matcher.end()));
                            }
                            for (String mline : lines) {
                                textGraphics.putString(0, offset, mline, SGR.BOLD);
                                offset++;
                            }
                        } else {
                            textGraphics.putString(0, offset, line, SGR.BOLD);
                            offset++;
                        }
                    }
                }
            } else {
                if(showMenu) {
                    textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    textGraphics.drawLine(0, 2, (terminal.getTerminalSize().getColumns() - 1), 2, '–');
                    String playersString = "Players";
                    String settingsString = "Settings";
                    if(choosedAction == 1) settingsString = "> Settings <";
                    if(choosedAction == 0) {
                        textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                        textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                        playersString = "> Players <";
                    } else {
                        textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    }
                    textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)-(playersString.length())-(settingsString.length()/2)-3, 3, playersString, SGR.BOLD);
                    textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)-(settingsString.length()/2)-2, 3, "|", SGR.BOLD);
                    if(choosedAction == 1) {
                        textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                        textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                    } else {
                        textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    }
                    textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)-(settingsString.length()/2), 3, settingsString, SGR.BOLD);
                    textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)+(settingsString.length()/2)+1, 3, "|", SGR.BOLD);
                    String backString = "Back";
                    if(choosedAction == 2) {
                        textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                        textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                        backString = "> Back <";
                    } else {
                        textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    }
                    textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)+(settingsString.length()/2)+3, 3, backString, SGR.BOLD);
                    textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    textGraphics.drawLine(0, 4, (terminal.getTerminalSize().getColumns() - 1), 4, '–');
                    if(choosedAction == 0) {
                        textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                        String totalOnline = "Total: "+Main.instance.room.getPlayers()+" players";
                        textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)-(totalOnline.length()/2), 5, totalOnline, SGR.BOLD);
                        int maxRows = Math.max(0, terminal.getTerminalSize().getRows()-5);
                        boolean choosedExists = false;
                        int choosed = 0;
                        int wrapNick = terminal.getTerminalSize().getColumns()/3;
                        List<GameClient> players = null;
                        if(maxRows > 0) {
                            players = Main.instance.room.clients.subList(0, Math.min(Main.instance.room.clients.size(), maxRows));
                            if(choosedActionInMenu > players.size()-1) choosedActionInMenu = players.size()-1;
                            int offset = 1;
                            for(GameClient cl : players) {
                                String nick = cl.getNickname().trim();
                                if(cl.getAccount() != null && cl.getAccount().persistentName != null) nick = cl.getAccount().persistentName.trim();
                                nick = nick.substring(0, Math.min(nick.trim().length(), wrapNick));
                                if(cl.isSpectating()) {
                                    if(nick.length()+(" (SPECTATOR)".length()) > wrapNick) {
                                        nick = nick.substring(0, wrapNick-(" (SPECTATOR)".length()));
                                        nick += " (SPECTATOR)";
                                    }
                                }
                                if(choosedActionInMenu == offset-1 && inMenuTab) {
                                    textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                                    textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                                    choosedExists = true;
                                    choosed = offset-1;
                                } else {
                                    textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                                }
                                textGraphics.putString(0, 5+offset, offset+". "+nick, SGR.BOLD);
                                offset++;
                            }
                        }
                        textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                        if(choosedExists) {
                            textGraphics.drawLine(5+wrapNick, 6, 5+wrapNick, 6+maxRows, '|');
                            GameClient choosedClient = players.get(choosed);
                            boolean hasAccount = choosedClient.getAccount() != null;
                            int offset = 0;
                            if(hasAccount) {
                                textGraphics.putString(5+wrapNick+2, 6+offset, "ID: "+choosedClient.getAccount().id, SGR.BOLD);
                                offset++;
                                textGraphics.putString(5+wrapNick+2, 6+offset, "Admin Level: "+choosedClient.getAccount().admin+(choosedClient.getAccount().limited == 1 ? " (Limited)" : ""), SGR.BOLD);
                                offset++;
                                textGraphics.putString(5+wrapNick+2, 6+offset, "Coins: "+choosedClient.getAccount().coins, SGR.BOLD);
                                offset++;
                            } else {
                                textGraphics.putString(5+wrapNick+2, 6+offset, "No Game Account", SGR.BOLD);
                                offset++;
                            }
                            offset++;
                            int offset3 = 25;
                            if (!choosedClient.isSpectating()) {
                                String anitext = "Animal: "+choosedClient.getPlayer().getInfo().getAnimalType()+":"+choosedClient.getPlayer().getInfo().getSkin() + " ("+choosedClient.getPlayer().getInfo().getType().name()+") (Tier "+choosedClient.getPlayer().getInfo().getTier()+")";
                                textGraphics.putString(5+wrapNick+2, 6+offset, anitext, SGR.BOLD);
                                offset++;
                                if(anitext.length() > offset3) offset3 = anitext.length()+3;
                            } else {
                                textGraphics.putString(5+wrapNick+2, 6+offset, "SPECTATOR", SGR.BOLD);
                                offset++;
                            }
                            // actions
                            textGraphics.drawLine(5+wrapNick+offset3, 6, 5+wrapNick+offset3, 6+maxRows, '|');
                            textGraphics.putString(5+wrapNick+offset3+2, 6, "Actions:", SGR.BOLD);
                            int offset4 = 0;
                            boolean canKick = true;
                            boolean canBan = true;
                            if(canKick) {
                                if(choosedActionInTab == 1 && choosedActionInTab2 == offset4 && inMenuTab) {
                                    textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                                    textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                                    choosedActionInPlayer = 1;
                                } else {
                                    textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                                }
                                textGraphics.putString(5+wrapNick+offset3+2, 7+offset4, "- Kick", SGR.BOLD);
                                textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                                textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                                offset4++;
                            }
                            if(canBan) {
                                if(choosedActionInTab == 1 && choosedActionInTab2 == offset4 && inMenuTab) {
                                    textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                                    textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                                    choosedActionInPlayer = 2;
                                } else {
                                    textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                                }
                                textGraphics.putString(5+wrapNick+offset3+2, 7+offset4, "- Ban", SGR.BOLD);
                                textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                                textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                                offset4++;
                            }
                        }
                    } else if(choosedAction == 2) {
                        textGraphics.setForegroundColor(TextColor.ANSI.MAGENTA);
                        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                        String retuString = "Press ENTER to return to main menu!";
                        textGraphics.putString(((terminal.getTerminalSize().getColumns() - 1)/2)-(retuString.length()/2), 5, retuString, SGR.BOLD);
                    }
                } else if(showFullStatistic) {
                    long usedmemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                    textGraphics.putString(0, 2, "Total Memory: " + Utils.humanReadableByteCountSI(Runtime.getRuntime().totalMemory()), SGR.BOLD);
                    textGraphics.putString(0, 3, "Max Memory: " + Utils.humanReadableByteCountSI(Runtime.getRuntime().maxMemory()), SGR.BOLD);
                    textGraphics.putString(0, 4, "Used Memory: " + Utils.humanReadableByteCountSI(usedmemory), SGR.BOLD);
                    textGraphics.putString(0, 5, "Free Memory: " + Utils.humanReadableByteCountSI(Runtime.getRuntime().freeMemory()), SGR.BOLD);
                    textGraphics.putString(0, 6, "Latest Collision Math Time (LT): " + Main.instance.room.latestTick, SGR.BOLD);
                    textGraphics.putString(0, 7, "Highest Collision Math Time (HT): " + Main.instance.room.higherTick, SGR.BOLD);
                    textGraphics.putString(0, 8, "Total Objects on GameMap: " + Main.instance.room.getObjects().size(), SGR.BOLD);
                    textGraphics.putString(0, 9, "Server Uptime: " + Main.instance.room.upTime(), SGR.BOLD);
                    textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
                    textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
                    textGraphics.putString(0, terminal.getTerminalSize().getRows() - 1, "> BACK <", SGR.BOLD);
                    textGraphics.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
                    textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                }
            }
            terminal.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Timer updateTimer = new Timer(1500);

    public void update() {
        updateTimer.update(Constants.TICKS_PER_SECOND);
        if(updateTimer.isDone()) {
            draw();
            updateTimer.reset();
        }
        try {
            KeyStroke keyStroke = terminal.pollInput();
            if(keyStroke != null) {
                switch(keyStroke.getKeyType()) {
                    case Escape:
                        try {
                            terminal.putCharacter('\n');
                            terminal.clearScreen();
                            terminal.close();
                            System.exit(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ArrowLeft:
                        if(!showFullStatistic) {
                            if(showMenu) {
                                if(!inMenuTab) {
                                    choosedAction--;
                                    if(choosedAction < 0) choosedAction = 2;
                                } else {
                                    choosedActionInTab--;
                                    if(choosedActionInTab < 0) choosedActionInTab = 1;
                                }
                            } else {
                                choosedAction--;
                                if(choosedAction < 0) choosedAction = 1;
                            }
                        }
                        updateTimer.reset();
                        draw();
                        break;
                    case ArrowRight:
                        if(!showFullStatistic) {
                            if(showMenu) {
                                if(!inMenuTab) {
                                    choosedAction++;
                                    if(choosedAction > 2) choosedAction = 0;
                                } else {
                                    choosedActionInTab++;
                                    if(choosedActionInTab > 1) choosedActionInTab = 0;
                                }
                            } else {
                                choosedAction++;
                                if(choosedAction > 1) choosedAction = 0;
                            }
                        }
                        updateTimer.reset();
                        draw();
                        break;
                    case ArrowUp:
                        if(showMenu) {
                            if(inMenuTab) {
                                if(choosedActionInTab > 0) {
                                    choosedActionInTab2--;
                                    if(choosedActionInTab2 < 0) choosedActionInTab2 = 1;
                                } else {
                                    if(choosedActionInMenu-1 < 0) {
                                        inMenuTab = false;
                                    } else choosedActionInMenu--;
                                }
                            }
                        }
                        updateTimer.reset();
                        draw();
                        break;
                    case ArrowDown:
                        if(showMenu) {
                            if(!inMenuTab) {
                                inMenuTab = true;
                            } else {
                                if(choosedActionInTab > 0) {
                                    choosedActionInTab2++;
                                    if(choosedActionInTab2 > 1) choosedActionInTab2 = 0;
                                } else {
                                    choosedActionInMenu++;
                                }
                            }
                        }
                        updateTimer.reset();
                        draw();
                        break;
                    case Enter:
                        if(!showFullStatistic && !showMenu) {
                            if(choosedAction == 0) {
                                showMenu = true;
                            }
                            if(choosedAction == 1) {
                                showFullStatistic = true;
                            }
                        } else if(showFullStatistic) {
                            showFullStatistic = false;
                        } else if(showMenu) {
                            if(choosedAction == 2) {
                                showMenu = false;
                                choosedAction = 0;
                            }
                        }
                        updateTimer.reset();
                        draw();
                        break;
                    default:
                        // g_log.add("Pressed: "+keyStroke.getCharacter());
                        updateTimer.reset();
                        draw();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}