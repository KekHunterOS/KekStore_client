package com.team420.kekstore.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.apache.commons.io.IOUtils;
import com.team420.kekstore.R;
import com.team420.kekstore.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class DBHelperTest {
    static final String TAG = "DBHelperTest";

    private List<String> getReposFromXml(String xml) throws IOException, XmlPullParserException {
        File additionalReposXml = File.createTempFile("." + context.getPackageName() + "-DBHelperTest_",
                "_additional_repos.xml");
        Log.i(TAG, "additionalReposXml: " + additionalReposXml);

        FileOutputStream outputStream = new FileOutputStream(additionalReposXml);
        outputStream.write(xml.getBytes());
        outputStream.close();

        // Now parse that xml file
        return DBHelper.parseAdditionalReposXml(additionalReposXml);
    }

    protected Context context;

    @Before
    public final void setupBase() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void parseAdditionalReposXmlAllOneLineTest() throws IOException, XmlPullParserException {
        String oneRepoXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>" +
                "<string-array name=\"default_repos\">\n" +
                "<!-- name -->" +
                "<item>F-Droid</item>" +
                "<!-- address -->" +
                "<item>https://f-droid.org/repo</item>" +
                "<!-- description -->" +
                "<item>The official F-Droid repository. Applications in this repository are mostly built" +
                "directory from the source code. Some are official binaries built by the original" +
                "application developers - these will be replaced by source-built versions over time." +
                "</item>" +
                "<!-- version -->" +
                "<item>13</item>" +
                "<!-- enabled -->" +
                "<item>1</item>" +
                "<!-- push requests -->" +
                "<item>ignore</item>" +
                "<!-- pubkey -->" +
                "<item>" +
                "3082035e30820246a00302010202044c49cd00300d06092a864886f70d01010505003071310b30090603550406130255" +
                "</item>" +
                "</string-array>" +
                "</resources>";
        List<String> repos = getReposFromXml(oneRepoXml);
        assertEquals("Should contain one repo's worth of items", DBHelper.REPO_XML_ITEM_COUNT, repos.size());
    }

    @Test
    public void parseAdditionalReposXmlIncludedPriorityTest() throws IOException, XmlPullParserException {
        String wrongXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<resources>" +
                "<string-array name=\"default_repos\">\n" +
                "<!-- name -->" +
                "<item>F-Droid</item>" +
                "<!-- address -->" +
                "<item>https://f-droid.org/repo</item>" +
                "<!-- description -->" +
                "<item>The official F-Droid repository. Applications in this repository are mostly built" +
                "directory from the source code. Some are official binaries built by the original" +
                "application developers - these will be replaced by source-built versions over time." +
                "</item>" +
                "<!-- version -->" +
                "<item>13</item>" +
                "<!-- enabled -->" +
                "<item>1</item>" +
                "<!-- priority -->" +
                "<item>1</item>" +
                "<!-- push requests -->" +
                "<item>ignore</item>" +
                "<!-- pubkey -->" +
                "<item>" +
                "3082035e30820246a00302010202044c49cd00300d06092a864886f70d01010505003071310b3009060355040613025" +
                "</item>" +
                "</string-array>" +
                "</resources>";
        getReposFromXml(wrongXml);
        List<String> repos = getReposFromXml(wrongXml);
        assertEquals("Should be empty", 0, repos.size());
    }

    @Test(expected = XmlPullParserException.class)
    public void parseAdditionalReposXmlDoubleTagTest() throws IOException, XmlPullParserException {
        String wrongXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<resources>"
                + "<string-array name=\"additional_repos\">"
                + "<!-- address -->"
                + "<item><item>https://www.oem0.com/yeah/repo</item>"
                + "<!-- description -->"
                + "<item>I'm the first oem repo.</item>"
                + "<!-- version -->"
                + "<item>22</item>"
                + "<!-- enabled -->"
                + "<item>1</item>"
                + "<!-- push requests -->"
                + "<item>ignore</item>"
                + "<!-- pubkey -->"
                + "<item>fffff2313aaaaabcccc111</item>"
                + "</string-array>"
                + "</resources>";
        getReposFromXml(wrongXml);
        fail("Invalid xml read successfully --> Wrong");
    }

    @Test(expected = XmlPullParserException.class)
    public void parseAdditionalReposXmlMissingStartTagTest() throws IOException, XmlPullParserException {
        String wrongXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<item>https://www.oem0.com/yeah/repo</item>"
                + "<!-- description -->"
                + "<item>I'm the first oem repo.</item>"
                + "<!-- version -->"
                + "<item>22</item>"
                + "<!-- enabled -->"
                + "<item>1</item>"
                + "<!-- push requests -->"
                + "<item>ignore</item>"
                + "<!-- pubkey -->"
                + "<item>fffff2313aaaaabcccc111</item>"
                + "</string-array>"
                + "</resources>";
        getReposFromXml(wrongXml);
        fail("Invalid xml read successfully --> Wrong");
    }

    @Test
    public void parseAdditionalReposXmlWrongCountTest() throws IOException, XmlPullParserException {
        String wrongXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<resources>"
                + "<string-array name=\"default_repos\"><item>foo</item></string-array>"
                + "</resources>";
        List<String> repos = getReposFromXml(wrongXml);
        assertEquals("Should be empty", 0, repos.size());
    }

    /**
     * Parse valid xml but make sure that only <item> tags are parsed
     */
    @Test
    public void parseAdditionalReposXmlSloppyTest() throws IOException, XmlPullParserException {
        InputStream input = TestUtils.class.getClassLoader().getResourceAsStream("ugly_additional_repos.xml");
        String validXml = IOUtils.toString(input, "UTF-8");

        List<String> repos = getReposFromXml(validXml);
        assertEquals(2 * DBHelper.REPO_XML_ITEM_COUNT, repos.size());
        assertEquals("Repo Name", repos.get(8));
        assertEquals("https://www.oem0.com/yeah/repo", repos.get(9));
    }

    @Test
    public void parseAdditionalReposXmlPositiveTest() throws IOException {
        InputStream input = TestUtils.class.getClassLoader().getResourceAsStream("additional_repos.xml");
        String reposXmlContent = IOUtils.toString(input, "UTF-8");

        List<String> additionalRepos;
        try {
            additionalRepos = getReposFromXml(reposXmlContent);
        } catch (IOException io) {
            fail("IOException. Failed parsing xml string into repos.");
            return;
        } catch (XmlPullParserException xppe) {
            fail("XmlPullParserException. Failed parsing xml string into repos.");
            return;
        }

        // We should have loaded these repos
        List<String> oem0 = Arrays.asList(
                "oem0Name",
                "https://www.oem0.com/yeah/repo",
                "I'm the first oem repo.",
                "22",
                "1",
                "0",  // priority is inserted by DBHelper.parseAdditionalReposXml()
                "ignore",
                "fffff2313aaaaabcccc111");
        List<String> oem1 = Arrays.asList(
                "oem1MyNameIs",
                "https://www.mynameis.com/rapper/repo",
                "Who is the first repo?",
                "22",
                "0",
                "0",  // priority is inserted by DBHelper.parseAdditionalReposXml()
                "ignore",
                "ddddddd2313aaaaabcccc111");
        List<String> shouldBeRepos = new LinkedList<>();
        shouldBeRepos.addAll(oem0);
        shouldBeRepos.addAll(oem1);

        assertEquals(additionalRepos.size(), shouldBeRepos.size());
        for (int i = 0; i < additionalRepos.size(); i++) {
            assertEquals(shouldBeRepos.get(i), additionalRepos.get(i));
        }
    }

    @SuppressWarnings("LineLength")
    @Test
    public void canAddAdditionalRepos() throws IOException {
        File oemEtcDir = new File("/oem/etc");
        File oemEtcPackageDir = new File(oemEtcDir, context.getPackageName());
        if (!oemEtcPackageDir.canWrite() || !oemEtcDir.canWrite()) {
            if (oemEtcDir.canWrite() || new File("/").canWrite()) {
                oemEtcPackageDir.mkdirs();
            }
            if (TextUtils.isEmpty(System.getenv("CI")) && !oemEtcPackageDir.isDirectory()) {
                Log.e(TAG, "Cannot create " + oemEtcDir + ", skipping test!");
                return;
            }
        }

        File additionalReposXmlFile = new File(oemEtcPackageDir, "additional_repos.xml");
        FileOutputStream outputStream = new FileOutputStream(additionalReposXmlFile);
        outputStream.write(("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<resources>"
                + "<string-array name=\"default_repos\">"
                + "<!-- name -->"
                + "<item>oem0Name</item>"
                + "<!-- address -->"
                + "<item>https://www.oem0.com/yeah/repo</item>"
                + "<!-- description -->"
                + "<item>I'm the first oem repo.</item>"
                + "<!-- version -->"
                + "<item>22</item>"
                + "<!-- enabled -->"
                + "<item>1</item>"
                + "<!-- push requests -->"
                + "<item>ignore</item>"
                + "<!-- pubkey -->"
                + "<item>fffff2313aaaaabcccc111</item>"

                + "<!-- name -->"
                + "<item>oem1MyNameIs</item>"
                + "<!-- address -->"
                + "<item>https://www.mynameis.com/rapper/repo</item>"
                + "<!-- description -->"
                + "<item>Who is the first repo?</item>"
                + "<!-- version -->"
                + "<item>22</item>"
                + "<!-- enabled -->"
                + "<item>0</item>"
                + "<!-- push requests -->"
                + "<item>ignore</item>"
                + "<!-- pubkey -->"
                + "<item>ddddddd2313aaaaabcccc111</item>"
                + "</string-array>"
                + "</resources>").getBytes());
        outputStream.close();

        try {
            List<String> initialRepos = DBHelper.loadInitialRepos(context);

            // Construct the repos that we should have loaded
            List<String> oem0 = Arrays.asList("oem0Name", "https://www.oem0.com/yeah/repo", "I'm the first oem repo.",
                    "22", "1", "0", "ignore", "fffff2313aaaaabcccc111");
            List<String> oem1 = Arrays.asList("oem1MyNameIs", "https://www.mynameis.com/rapper/repo", "Who is the first repo?",
                    "22", "0", "0", "ignore", "ddddddd2313aaaaabcccc111");

            String[] defaultRepos = context.getResources().getStringArray(R.array.default_repos);

            List<String> shouldBeRepos = new LinkedList<>();
            shouldBeRepos.addAll(oem0);
            shouldBeRepos.addAll(oem1);
            shouldBeRepos.addAll(Arrays.asList(defaultRepos));

            // Normalize whitespace in descriptions, just like DBHelper does.
            final int descriptionIndex = 2;
            for (int i = descriptionIndex; i < shouldBeRepos.size(); i += DBHelper.REPO_XML_ITEM_COUNT) {
                String description = shouldBeRepos.get(i);
                shouldBeRepos.set(i, description.replaceAll("\\s+", " "));
            }

            for (int i = 0; i < initialRepos.size(); i++) {
                assertEquals(shouldBeRepos.get(i), initialRepos.get(i));
            }
        } finally {
            for (Repo repo : RepoProvider.Helper.all(context, new String[]{Schema.RepoTable.Cols._ID})) {
                RepoProvider.Helper.remove(context, repo.getId());
            }
            additionalReposXmlFile.delete();
            DBHelper.clearDbHelperSingleton();
        }
    }
}
