package controller.ui;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import data.GameControlData;
import data.Rules;
import data.SPLDynamicBallHandling;
import data.SPLPenaltyShootout;
import data.Teams;


/**
 * @author Michel Bartsch
 *
 * This is only to be on starting the program to get starting input.
 */
public class StartInput extends JFrame implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Some constants defining this GUI`s appearance as their names say.
     * Feel free to change them and see what happens.
     */
    private static final String WINDOW_TITLE = "GameController";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 450;
    private static final int STANDARD_SPACE = 10;
    private static final int TEAMS_HEIGHT = 300;
    private static final int IMAGE_SIZE = 250;
    private static final int OPTIONS_CONTAINER_HEIGHT = 80;
    private static final int OPTIONS_HEIGHT = 22;
    private static final int START_HEIGHT = 30;
    /** This is not what the name says ;) */
    private static final int FULLSCREEN_WIDTH = 160;
    private static final String ICONS_PATH = "config/icons/";
    private static final String BACKGROUND_EXT = ".png";
    private static final String FULLTIME_LABEL_NO = "Preliminaries Game";
    private static final String FULLTIME_LABEL_YES = "Play-off Game";
    private static final String FULLSCREEN_LABEL = "Fullscreen";
    private static final String START_LABEL = "Start";
    private static final String TEAM_COLOR_CHANGE = "Color";

    /** If true, this GUI has finished and offers it`s input. */
    public boolean finished = false;

    /** The inputs that can be read from this GUI when it has finished. */
    public int[] outTeam = {0, 0};
    public byte[] outTeamColor = new byte[2];
    public boolean outFulltime;
    public boolean outFullscreen;

    /** All the components of this GUI. */
    private ImagePanel[] teamContainer = new ImagePanel[2];
    private JPanel[] teamChooseContainer = new JPanel[2];
    private ImageIcon[] teamIcon = new ImageIcon[2];
    private JButton[] teamColorChange = new JButton[2];
    private JLabel[] teamIconLabel = new JLabel[2];
    @SuppressWarnings("unchecked")
    private JComboBox<String>[] team = (JComboBox<String>[]) new JComboBox[2];
    private JPanel optionsLeft;
    private JPanel optionsRight;
    private JComboBox<String> league;
    private JRadioButton nofulltime;
    private JRadioButton fulltime;
    private ButtonGroup fulltimeGroup;
    private Checkbox fullscreen;
    private JButton start;

    private String[][] colorNames = new String[2][];

    private HashMap<String, Image> images = new HashMap<String, Image>();

    /**
     * @author Felix Wege
     *
     * The set of preselection values for the game type.
     */
    public enum GameType
    {
        UNDEFINED, PRELIMINARY, PLAYOFF
    }

    /**
     * Creates a new StartInput.
     * @param fullscreenMode The preset value of the fullscreen checkbox.
     * @param gameType The game type (either UNDEFINED, PRELIMINARY or PLAYOFF).
     * @param presetTeams The preset team numbers.
     * @param onlyTeamsAndColors Whether only the teams and their colors should be selectable.
     */
    public StartInput(boolean fullscreenMode, final GameType gameType, final int[] presetTeams, boolean onlyTeamsAndColors)
    {
        super(WINDOW_TITLE);

        final Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation((int) center.getX() - WINDOW_WIDTH / 2, (int) center.getY() - WINDOW_HEIGHT / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, STANDARD_SPACE));

        String[] teams = getShortTeams();
        outTeam = presetTeams;
        for (int i=0; i<2; i++) {
            team[i] = new JComboBox<>(teams);
            for (int j=0; j<teams.length; ++j) {
                if (Integer.parseInt(teams[j].split(" \\(")[1].split("\\)")[0]) == outTeam[i]) {
                    team[i].setSelectedIndex(j);
                    break;
                }
            }
            teamColorChange[i] = new JButton(TEAM_COLOR_CHANGE);
            reloadTeamColor(i, i == 1);
            teamContainer[i] = new ImagePanel(getImage(i, colorNames[i][0]));
            teamContainer[i].setPreferredSize(new Dimension(WINDOW_WIDTH/2-STANDARD_SPACE, TEAMS_HEIGHT));
            teamContainer[i].setOpaque(true);
            teamContainer[i].setLayout(new BorderLayout());
            add(teamContainer[i]);
            setTeamIcon(i, 0);
            teamIconLabel[i] = new JLabel(teamIcon[i]);
            teamContainer[i].add(teamIconLabel[i], BorderLayout.CENTER);
            teamChooseContainer[i] = new JPanel(new BorderLayout());
            teamContainer[i].add(teamChooseContainer[i], BorderLayout.SOUTH);
            teamChooseContainer[i].add(team[i], BorderLayout.CENTER);
        }
        teamChooseContainer[0].add(teamColorChange[0], BorderLayout.WEST);
        teamChooseContainer[1].add(teamColorChange[1], BorderLayout.EAST);

        teamColorChange[0].addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    switchTeamColor(0);
                    reloadTeamColor(1, true);
                    updateBackgrounds();
                    teamIconLabel[0].repaint();
                    teamIconLabel[1].repaint();
                    startEnabling();
                }
            }
        );
        teamColorChange[1].addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    switchTeamColor(1);
                    reloadTeamColor(0, true);
                    updateBackgrounds();
                    teamIconLabel[0].repaint();
                    teamIconLabel[1].repaint();
                    startEnabling();
                }
            }
        );

        team[0].addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    Object selected = team[0].getSelectedItem();
                    if (selected == null) {
                        return;
                    }
                    outTeam[0] = Integer.parseInt(((String)selected).split(" \\(")[1].split("\\)")[0]);
                    reloadTeamColor(0, false);
                    reloadTeamColor(1, true);
                    updateBackgrounds();
                    setTeamIcon(0, outTeam[0]);
                    teamIconLabel[0].setIcon(teamIcon[0]);
                    teamIconLabel[0].repaint();
                    teamIconLabel[1].repaint();
                    startEnabling();
                }
            }
        );
        team[1].addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    Object selected = team[1].getSelectedItem();
                    if (selected == null){
                        return;
                    }
                    outTeam[1] = Integer.parseInt(((String)selected).split(" \\(")[1].split("\\)")[0]);
                    reloadTeamColor(0, false);
                    reloadTeamColor(1, true);
                    updateBackgrounds();
                    setTeamIcon(1, outTeam[1]);
                    teamIconLabel[1].setIcon(teamIcon[1]);
                    teamIconLabel[0].repaint();
                    teamIconLabel[1].repaint();
                    startEnabling();
                }
            }
        );

        optionsLeft = new JPanel();
        optionsLeft.setPreferredSize(new Dimension(WINDOW_WIDTH/2-2*STANDARD_SPACE, OPTIONS_CONTAINER_HEIGHT));
        optionsLeft.setLayout(new FlowLayout(FlowLayout.CENTER));
        add(optionsLeft);

        JPanel fullscreenPanel = new JPanel();
        fullscreenPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        optionsLeft.add(fullscreenPanel);

        fullscreen = new Checkbox(FULLSCREEN_LABEL);
        fullscreen.setPreferredSize(new Dimension(FULLSCREEN_WIDTH, OPTIONS_HEIGHT));
        fullscreen.setState(fullscreenMode);
        fullscreenPanel.add(fullscreen);

        optionsRight = new JPanel();
        optionsRight.setPreferredSize(new Dimension(WINDOW_WIDTH/2-2*STANDARD_SPACE, OPTIONS_CONTAINER_HEIGHT));
        add(optionsRight);
        Dimension optionsDim = new Dimension(WINDOW_WIDTH/3-2*STANDARD_SPACE, OPTIONS_HEIGHT);
        league = new JComboBox<String>();
        for (int i=0; i < Rules.LEAGUES.length; i++) {
            league.addItem(Rules.LEAGUES[i].leagueName);
            if (Rules.LEAGUES[i] == Rules.league) {
                league.setSelectedIndex(i);
            }
        }
        league.setPreferredSize(optionsDim);
        league.addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (e != null) { // not initial setup
                        for (int i=0; i < Rules.LEAGUES.length; i++) {
                            if (Rules.LEAGUES[i].leagueName.equals((String)league.getSelectedItem())) {
                                Rules.league = Rules.LEAGUES[i];
                                break;
                            }
                        }
                    }
                    nofulltime.setVisible(!onlyTeamsAndColors && !(Rules.league instanceof SPLPenaltyShootout || Rules.league instanceof SPLDynamicBallHandling));
                    fulltime.setVisible(!onlyTeamsAndColors && !(Rules.league instanceof SPLPenaltyShootout || Rules.league instanceof SPLDynamicBallHandling));
                    nofulltime.setText(FULLTIME_LABEL_NO);
                    fulltime.setText(FULLTIME_LABEL_YES);
                    if (gameType == GameType.PRELIMINARY) {
                        nofulltime.setSelected(true);
                    } else if (gameType == GameType.PLAYOFF) {
                        fulltime.setSelected(true);
                    }
                    teamColorChange[0].setVisible(true);
                    teamColorChange[1].setVisible(true);
                    showAvailableTeams();
                    startEnabling();
                }
            }
        );
        if (!onlyTeamsAndColors) {
            optionsRight.add(league);
        }
        nofulltime = new JRadioButton();
        nofulltime.setPreferredSize(optionsDim);
        fulltime = new JRadioButton();
        fulltime.setPreferredSize(optionsDim);
        fulltimeGroup = new ButtonGroup();
        fulltimeGroup.add(nofulltime);
        fulltimeGroup.add(fulltime);
        if (!onlyTeamsAndColors) {
            optionsRight.add(nofulltime);
            optionsRight.add(fulltime);
        }
        nofulltime.addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    startEnabling();
                }});
        fulltime.addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    startEnabling();
                }});
        start = new JButton(START_LABEL);
        start.setPreferredSize(new Dimension(WINDOW_WIDTH/3-2*STANDARD_SPACE, START_HEIGHT));
        start.setEnabled(false);
        add(start);
        start.addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    outFulltime = fulltime.isSelected() && fulltime.isVisible();
                    outFullscreen = fullscreen.getState();
                    finished = true;
                }});

        league.getActionListeners()[league.getActionListeners().length - 1].actionPerformed(null);

        getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        pack();
        setVisible(true);
    }
    /** Show in the combo box which teams are available for the selected league and competition*/
    private void showAvailableTeams()
    {
        final String[] previousColors = {colorNames[0][0], colorNames[1][0]};
        String[] names = getShortTeams();
        for (int i=0; i < 2; i++) {
            final Object previousSelection = team[i].getSelectedItem();
            team[i].removeAllItems();
            boolean foundTeam = false;
            for (int j=0; j < names.length; j++) {
                team[i].addItem(names[j]);
                // Try to restore the previous selection (including its selected color).
                if (names[j].equals(previousSelection)) {
                    team[i].setSelectedIndex(j);
                    if (previousColors[i].equals(colorNames[i][1])) {
                        switchTeamColor(i);
                        updateBackgrounds();
                    }
                    foundTeam = true;
                }
            }
            if (!foundTeam) {
                outTeam[i] = 0;
            }
            setTeamIcon(i, outTeam[i]);
            teamIconLabel[i].setIcon(teamIcon[i]);
            teamIconLabel[i].repaint();
        }
    }

    /**
     * Calculates an array that contains only the existing Teams of the
     * current league.
     *
     * @return  Short teams array with numbers
     */
    private String[] getShortTeams()
    {
        String[] fullTeams = Teams.getNames(true);
        String[] out;
        int k = 0;
        for (int j=0; j<fullTeams.length; j++) {
            if (fullTeams[j] != null) {
                k++;
            }
        }
        out = new String[k];
        k = 0;
        for (int j=0; j<fullTeams.length; j++) {
            if (fullTeams[j] != null) {
                out[k++] = fullTeams[j];
            }
        }

        Arrays.sort(out, 1, out.length, String.CASE_INSENSITIVE_ORDER);

        return out;
    }

    /**
     * Sets the Team-Icon on the GUI.
     *
     * @param side      The side (0=left, 1=right)
     * @param team      The number of the Team
     */
    private void setTeamIcon(int side, int team)
    {
        teamIcon[side] = new ImageIcon(Teams.getIcon(team));
        float scaleFactor;
        if (teamIcon[side].getImage().getWidth(null) > teamIcon[side].getImage().getHeight(null)) {
            scaleFactor = (float)IMAGE_SIZE/teamIcon[side].getImage().getWidth(null);
        } else {
            scaleFactor = (float)IMAGE_SIZE/teamIcon[side].getImage().getHeight(null);
        }

        // getScaledInstance/SCALE_SMOOTH does not work with all color models, so we need to convert image
        BufferedImage image = (BufferedImage) teamIcon[side].getImage();
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = temp.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = temp;
        }

        teamIcon[side].setImage(image.getScaledInstance(
                (int)(teamIcon[side].getImage().getWidth(null)*scaleFactor),
                (int)(teamIcon[side].getImage().getHeight(null)*scaleFactor),
                Image.SCALE_SMOOTH));
    }

    /**
     * Enables the start button, if the conditions are ok.
     */
    private void startEnabling()
    {
        start.setEnabled(outTeam[0] != outTeam[1] &&
                outTeamColor[0] != outTeamColor[1] &&
                (fulltime.isSelected() || nofulltime.isSelected() || !fulltime.isVisible()));
    }

    private Image getImage(int side, String color)
    {
        String filename = Rules.league.leagueName + "/" + side + "/" + color;
        if (images.get(filename) == null) {

            // Default color when color is not available in Rules.league.teamColor:
            Color c = Color.WHITE;

            // Search the Java.awt.Color-Class for Color-Name:
            for(int i=0; i<Rules.league.teamColorName.length; i++){
                if(Rules.league.teamColorName[i].toLowerCase().equals(color.toLowerCase())){
                    c = Rules.league.teamColor[i];
                    break;
                }
            }

            Image image = new BackgroundImage(ICONS_PATH + Rules.league.leagueDirectory + BACKGROUND_EXT, side==1, c).getImage();
            images.put(filename, image);
        }
        return images.get(filename);
    }

    private void switchTeamColor(final int team)
    {
        String tmpColorString = colorNames[team][0];
        colorNames[team][0] = colorNames[team][1];
        colorNames[team][1] = tmpColorString;
        updateTeamColorIndicator(team);
    }

    private void updateTeamColorIndicator(final int team) {
        teamColorChange[team].setToolTipText(String.format("Change to alternative team color (%s)", Rules.league.teamColorName[fromColorName(colorNames[team][1])]));
        teamColorChange[team].setBackground(Rules.league.teamColor[fromColorName(colorNames[team][0])]);
    }

    private void reloadTeamColor(final int team, final boolean checkOther)
    {
        colorNames[team] = Teams.getColors(outTeam[team]).clone();
        if (colorNames[team] == null || colorNames[team].length == 0) {
            colorNames[team] = new String[]{"blue", "red"};
        } else if (colorNames[team].length == 1) {
            colorNames[team] = new String[]{colorNames[team][0], !"red".equals(colorNames[team][0]) ? "red" : "blue"};
        }
        if (checkOther && colorNames[team][0].equals(colorNames[1 - team][0])) {
            switchTeamColor(team);
        } else {
            updateTeamColorIndicator(team);
        }
    }

    private void updateBackgrounds()
    {
        for (int i = 0; i < 2; ++i) {
            teamContainer[i].setImage(getImage(i, colorNames[i][0]));
            outTeamColor[i] = fromColorName(colorNames[i][0]);
        }
    }

    private static byte fromColorName(final String colorName) {
        switch (colorName) {
            case "blue":
                return GameControlData.TEAM_BLUE;
            case "red":
                return GameControlData.TEAM_RED;
            case "yellow":
                return GameControlData.TEAM_YELLOW;
            case "black":
                return GameControlData.TEAM_BLACK;
            case "green":
                return GameControlData.TEAM_GREEN;
            case "orange":
                return GameControlData.TEAM_ORANGE;
            case "purple":
                return GameControlData.TEAM_PURPLE;
            case "brown":
                return GameControlData.TEAM_BROWN;
            case "gray":
                return GameControlData.TEAM_GRAY;
            default:
                return GameControlData.TEAM_WHITE;
        }
    }

    /**
     * @author Michel Bartsch
     *
     * This is a normal JPanel, but it has a background image.
     */
    class ImagePanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        /** The image that is shown in the background. */
        private Image image;

        /**
         * Creates a new ImagePanel.
         *
         * @param image     The Image to be shown in the background.
         */
        public ImagePanel(Image image)
        {
            this.image = image;
        }

        /**
         * Changes the background image.
         *
         * @param image     Changes the image to this one.
         */
        public void setImage(Image image)
        {
            this.image = image;
        }

        /**
         * Paints this Component, should be called automatically.
         *
         * @param g     This components graphical content.
         */
        @Override
        public void paintComponent(Graphics g)
        {
            if (super.isOpaque()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.drawImage(image, (getWidth()-image.getWidth(null))/2, 0, image.getWidth(null), image.getHeight(null), null);
        }
    }
}
