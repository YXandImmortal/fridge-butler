package com.yx.fridgebutler.util;

import com.wf.captcha.base.Captcha;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * 支持自定义背景色的 PNG 验证码
 * <p>
 * 基于 easy-captcha 1.6.2 的 SpecCaptcha 实现，添加了背景色设置能力。
 * </p>
 */
public final class ThemedSpecCaptcha extends Captcha {

    /** 亮色主题背景色 - #FFFFFF 纯白 */
    public static final int[] BG_LIGHT = new int[]{255, 255, 255};
    /** 暗色主题背景色 - #1E293B 深蓝灰 */
    public static final int[] BG_DARK = new int[]{30, 41, 59};

    private Color backgroundColor = Color.WHITE;

    public ThemedSpecCaptcha() {
    }

    public ThemedSpecCaptcha(int width, int height) {
        this();
        setWidth(width);
        setHeight(height);
    }

    public ThemedSpecCaptcha(int width, int height, int len) {
        this(width, height);
        setLen(len);
    }

    public ThemedSpecCaptcha(int width, int height, int len, Font font) {
        this(width, height, len);
        setFont(font);
    }

    /**
     * 设置背景色
     *
     * @param rgb RGB 数组，如 new int[]{245, 245, 245}
     */
    public void setBackground(int[] rgb) {
        this.backgroundColor = new Color(rgb[0], rgb[1], rgb[2]);
    }

    @Override
    public boolean out(OutputStream out) {
        return graphicsImage(textChar(), out);
    }

    @Override
    public String toBase64() {
        return toBase64("data:image/png;base64,");
    }

    private boolean graphicsImage(char[] strs, OutputStream out) {
        try {
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bi.getGraphics();
            // 填充背景
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, width, height);
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画干扰圆
            drawOval(2, g2d);
            // 画干扰线
            g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            drawBesselLine(1, g2d);
            // 画字符串
            g2d.setFont(getFont());
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int fW = width / strs.length;
            int fSp = (fW - (int) fontMetrics.getStringBounds("W", g2d).getWidth()) / 2;
            for (int i = 0; i < strs.length; i++) {
                g2d.setColor(color());
                int fY = height - ((height - (int) fontMetrics.getStringBounds(String.valueOf(strs[i]), g2d).getHeight()) >> 1);
                g2d.drawString(String.valueOf(strs[i]), i * fW + fSp + 3, fY - 3);
            }
            g2d.dispose();
            ImageIO.write(bi, "png", out);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
