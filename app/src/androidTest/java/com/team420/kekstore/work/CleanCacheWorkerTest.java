package com.team420.kekstore.work;

import android.app.Instrumentation;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.io.FileUtils;
import com.team420.kekstore.compat.FileCompatTest;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test cannot run on Robolectric unfortunately since it does not support
 * <p>
 * This is marked with {@link LargeTest} because it always fails on the emulator
 * tests on GitLab CI.  That excludes it from the test run there.
 */
@LargeTest
public class CleanCacheWorkerTest {
    public static final String TAG = "CleanCacheWorkerEmulatorTest";

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public WorkManagerTestRule workManagerTestRule = new WorkManagerTestRule();

    @Test
    public void testWorkRequest() throws ExecutionException, InterruptedException {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CleanCacheWorker.class).build();
        workManagerTestRule.workManager.enqueue(request).getResult();
        ListenableFuture<WorkInfo> workInfo = workManagerTestRule.workManager.getWorkInfoById(request.getId());
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.get().getState());
    }

    @Test
    public void testClearOldFiles() throws IOException, InterruptedException {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        File tempDir = FileCompatTest.getWriteableDir(instrumentation);
        assertTrue(tempDir.isDirectory());
        assertTrue(tempDir.canWrite());

        File dir = new File(tempDir, "F-Droid-test.clearOldFiles");
        FileUtils.deleteQuietly(dir);
        assertTrue(dir.mkdirs());
        assertTrue(dir.isDirectory());

        File first = new File(dir, "first");
        first.deleteOnExit();

        File second = new File(dir, "second");
        second.deleteOnExit();

        assertFalse(first.exists());
        assertFalse(second.exists());

        assertTrue(first.createNewFile());
        assertTrue(first.exists());

        Thread.sleep(7000);
        assertTrue(second.createNewFile());
        assertTrue(second.exists());

        CleanCacheWorker.clearOldFiles(dir, 3000); // check all in dir
        assertFalse(first.exists());
        assertTrue(second.exists());

        Thread.sleep(7000);
        CleanCacheWorker.clearOldFiles(second, 3000); // check just second file
        assertFalse(first.exists());
        assertFalse(second.exists());

        // make sure it doesn't freak out on a non-existent file
        File nonexistent = new File(tempDir, "nonexistent");
        CleanCacheWorker.clearOldFiles(nonexistent, 1);
        CleanCacheWorker.clearOldFiles(null, 1);
    }
}
