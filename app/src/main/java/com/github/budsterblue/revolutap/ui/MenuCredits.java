package com.github.budsterblue.revolutap.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.budsterblue.revolutap.util.ToolsTracker;

public class MenuCredits extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolsTracker.info("Opened credits");
        TextView textview = new TextView(this);
        textview.setMovementMethod(LinkMovementMethod.getInstance());
        setContentView(textview);
        ((ViewGroup.MarginLayoutParams) textview.getLayoutParams()).setMargins(20, 20, 20, 20);
		String credits =
				"<html><body><font color=\"white\">" +
						"<font size=\"4\"><u><b>Devs</b></u></font>" +
						"<font size=\"2\"><br/>" +
						"<b>Budsterblue</b>" +
						"<br/>Current maintainer of the fork." +
						"<br/><br/><b>Philip Peng</b> (Keripo)" +
						"<br/>Keripo is the project's lead developer/current maintainer and works on the game's timing engine, osu! Mod, and various other features/settings. He is currently a junior  studying Computer Engineering at University of Pennsylvania. You can contact Keripo via email at <i>k.darktiger@gmail.com</i> or via freenode's IRC channels: <i>#stepmania-devs #android #android-dev #ipodlinux #stwinglounge</i>" +
						"<br/><br/><b>Matthew Croop</b> (mcroop)" +
						"<br/>Matt worked on the game's graphics engine, holds mechanics, and various optimization stuff. He is currently a junior studying Computer Science at University of Pennsylvania. You can contact Matt via email at <i>matthewcroop@gmail.com</i>" +
						"<br/><br/><b>Yui Suveepattananont</b>" +
						"<br/>Yui created part of the game's original graphics. She is currently a sophomore studying Digital Media Design at University of Pennsylvania. You can contact Yui via email at <i>ksuvee@seas.upenn.edu</i>" +
						"</font>" +
						"<br/><br/><font size=\"4\"><u><b>Thanks</b></u></font>" +
						"<font size=\"2\">" +
						"<br/>Special thanks go to:" +
						"<br/>- All the members of the <a href=\"http://beatsportable.com/updates/\">Beats Portable Google group</a> who help out with testing" +
						"<br/>- All the forum users at <a href=\"http://beatsportable.com/forums/\">Beats Portable forums</a> who providing valuable feedback and suggestions" +
						"<br/>- All the IRC-goers at <i>#android-dev</i> for advice/suggestions with programming my first app" +
						"<br/>- All the IRC-goers at <i>#stepmania-devs</i> for helping me with the .sm/.dwi format and simulation" +
						"<br/>- All the devs and peppy at <a href=\"http://osu.ppy.sh/forum/\">osu! forums</a> for providing the .osu format specs (ask peppy for them)" +
						"<br/>- All the translators at <a href=\"http://crowdin.net/project/beats\">Beats crowdin project</a> for help spreading the word (localized!)" +
						"<br/>- All of you users for making this project worthwhile and fun!" +
						"</font>" +
						"<br/><br/><u><font size=\"4\"><b>Credits</b></u></font>" +
						"<font size=\"2\">" +
						"<br/>- App's font: <a href=\"https://www.dafont.com/squarefont.font\">dafont</a>" +
						"<br/>- Old Font in logo: <a href=\"https://www.dafont.com/happy-killer.font\">dafont</a>" +
						"<br/>- Double arrow in old logo: <a href=\"http://www.psych.ufl.edu/~vollmer/images/750px-Double-arrow.svg.png\">vollmer</a>" +
						"<br/>- Old File browser/popup icons: <a href=\"http://www.everaldo.com/crystal/\">Crystal Project</a>" +
						"<br/>- .sm file icon: <a href=\"http://www.stepmania.com/wiki/Downloads\">StepMania</a>" +
						"<br/>- .dwi file icon: <a href=\"http://dwi.ddruk.com/downloads.php\">Dance With Intensity</a>" +
						"<br/>- Old .osu file icon: <a href=\"http://osu.ppy.sh/images/head-left.jpg\">osu!</a>" +
						"<br/>- \"Blue\" background image: <a href=\"http://www.desktopwallpapers.in/images/wallpapers/computer-music-561760.jpeg\">desktopwallpapers</a>" +
						"<br/>- Old \"Red\" background image: <a href=\"http://media-wallpapers.theotaku.com/1280-by-800-368923-20090813034932.jpg\">theotaku</a>" +
						"<br/>- Old \"White\" background image: <a href=\"http://www.wallpaper4me.com/images/wallpapers/headphonecommander-406985.jpeg\">wallpaper4me</a>" +
						"<br/>  (based off the original from <a href=\"http://www.diverse.jp/download/d12-hiiragi-1920.jpg\">Diverse System</a>)" +
						"</font>" +
						"</font></body></html>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textview.setText(Html.fromHtml(credits, 0));
        } else {
            textview.setText(Html.fromHtml(credits));
        }
    }
}