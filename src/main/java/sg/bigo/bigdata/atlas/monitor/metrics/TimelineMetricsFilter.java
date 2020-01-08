package sg.bigo.bigdata.atlas.monitor.metrics;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sg.bigo.bigdata.atlas.monitor.Constants.WHITE_LIST_PATTERN_PREFIX;

public class TimelineMetricsFilter {
    private final static Logger LOG = LoggerFactory.getLogger(TimelineMetricsFilter.class);

    private static Set<String> whitelistedMetrics;
    private static Set<Pattern> whitelistedMetricPatterns;

    public synchronized static void initializeMetricFilter(String whitelistFile) {
        if (whitelistedMetrics == null) {
            LOG.info("Init metric filters, white list file: " + whitelistFile);
            whitelistedMetrics = new HashSet<String>();
            whitelistedMetricPatterns = new HashSet<Pattern>();
            readMetricWhitelistFromFile(whitelistFile);
        }
    }

    public synchronized static boolean acceptAttr(String attrName) {
        if (whitelistedMetrics != null && whitelistedMetrics.contains(attrName)) {
            return true;
        }
        if (whitelistedMetricPatterns != null) {
            for (Pattern p : whitelistedMetricPatterns) {
                Matcher m = p.matcher(attrName);
                if (m.find()) {
                    if (whitelistedMetrics != null) {
                        whitelistedMetrics.add(attrName);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * For test
     * @param whitelistedMetrics
     * @param whitelistedMetricPatterns
     */
    public synchronized static void init(Set<String> whitelistedMetrics, Set<Pattern> whitelistedMetricPatterns) {
        if (TimelineMetricsFilter.whitelistedMetrics == null) {
            TimelineMetricsFilter.whitelistedMetrics = whitelistedMetrics;
            TimelineMetricsFilter.whitelistedMetricPatterns = whitelistedMetricPatterns;
        }
    }

    private static void readMetricWhitelistFromFile(String whitelistFile) {
        FileInputStream fstream = null;
        BufferedReader br = null;
        String strLine;
        try {
            fstream = new FileInputStream(whitelistFile);
            br = new BufferedReader(new InputStreamReader(fstream));

            while ((strLine = br.readLine()) != null) {
                // case sensitive
                strLine = strLine.trim();
                if (strLine.startsWith("#") || StringUtils.isEmpty(strLine)) {
                    // ignore the line starting with '#' or empty
                    continue;
                }
                if (strLine.startsWith(WHITE_LIST_PATTERN_PREFIX)) {
                    whitelistedMetricPatterns.add(Pattern.compile(strLine.substring(WHITE_LIST_PATTERN_PREFIX.length())));
                } else {
                    whitelistedMetrics.add(strLine);
                }
            }
        } catch (IOException ioEx) {
            LOG.error("Unable to parse metric whitelist file", ioEx);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException e) {
                // NO OP
            }
        }

        LOG.info("Whitelisting[Case Insensitive] " + whitelistedMetrics.size() + " metrics");
        LOG.debug("Whitelisted metrics : " + Arrays.toString(whitelistedMetrics.toArray()));
    }

}
