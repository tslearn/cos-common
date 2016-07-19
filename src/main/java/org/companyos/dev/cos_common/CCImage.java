package org.companyos.dev.cos_common;

/**
 * Created by tianshuo on 16/7/19.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class CCImage{
  private static ImageReader getImageReaderByFile(ImageInputStream iis)  {
    try {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
      if (imageReaders != null && imageReaders.hasNext()) {
        return imageReaders.next();
      }
      else {
        return null;
      }
    }
    catch(Exception e) {
      return null;
    }
  }

  private static void setGraphics2DHint(Graphics2D graphics2D) {
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);

    graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
        RenderingHints.VALUE_COLOR_RENDER_QUALITY);

    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC);

    graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }

  /**
   * 将图片尺寸剪裁在一个范围内, 图片宽高比不变, 图片自动居中剪裁
   * @param srcImagePath  原图片绝对路径
   * @param toImagePath   目标图片绝对路径
   * @param refWidth      剪裁的宽度
   * @param refHeight     剪裁的高度
   * @throws IOException
   */
  public static void clipImage(String srcImagePath,String toImagePath,int refWidth,int refHeight) throws IOException{
    ImageInputStream imageInputStream = null;
    try{
      imageInputStream = ImageIO.createImageInputStream(new File(srcImagePath));
      ImageReader reader = getImageReaderByFile(imageInputStream);
      reader.setInput(imageInputStream, true);

      int rawWidth = reader.getWidth(0);
      int rawHeight = reader.getHeight(0);

      double rawRatio = (double)rawWidth / (double) rawHeight;
      double clipRatio = (double)refWidth / (double) refHeight;

      int clipX;
      int clipY;
      int clipWidth;
      int clipHeight;

      // convert fat image to thin
      if (rawRatio >  clipRatio) {
        clipY = 0;
        clipHeight = rawHeight;
        clipWidth = (int)(clipRatio * clipHeight);
        clipX = (rawWidth - clipWidth) / 2;
      }
      // convert thin image to fat
      else {
        clipX = 0;
        clipWidth = rawWidth;
        clipHeight = (int) (clipWidth / clipRatio);
        clipY = (rawHeight - clipHeight) / 2;
      }

      ImageReadParam param = reader.getDefaultReadParam();
      Rectangle rect = new Rectangle(clipX, clipY, clipWidth, clipHeight);
      param.setSourceRegion(rect);
      BufferedImage src = reader.read(0,param);
      String format = reader.getFormatName();

      // not  increase
      refWidth = Math.min(refWidth, clipWidth);
      refHeight = Math.min(refHeight, clipHeight);

      // 放大边长
      BufferedImage bi = new BufferedImage(refWidth, refHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics2D = (Graphics2D) bi.getGraphics();
      setGraphics2DHint(graphics2D);
      //绘制放大后的图片
      graphics2D.drawImage(src, 0, 0, refWidth, refHeight, null);
      ImageIO.write(bi, format, new File(toImagePath));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally{
      if (imageInputStream != null) {
        imageInputStream.close();
      }
    }
  }

  /**
   * 将图片尺寸限制在一个范围内, 图片宽高比不变
   * @param srcImagePath  原图片绝对路径
   * @param toImagePath   目标图片绝对路径
   * @param limitWidth         限制范围的宽度
   * @param limitHeight        限制范围的高度
   * @throws IOException
   */
  public static void limitImage(String srcImagePath,String toImagePath,int limitWidth,int limitHeight) throws IOException{
    ImageInputStream imageInputStream = null;
    try{
      imageInputStream = ImageIO.createImageInputStream(new File(srcImagePath));
      ImageReader reader = getImageReaderByFile(imageInputStream);
      reader.setInput(imageInputStream, true);

      int rawWidth = reader.getWidth(0);
      int rawHeight = reader.getHeight(0);

      double rawRatio = (double)rawWidth / (double) rawHeight;
      double limitRatio = (double)limitWidth / (double) limitHeight;

      int width;
      int height;

      // convert fat image to thin
      if (rawRatio >  limitRatio) {
        width = Math.min(rawWidth, limitWidth);
        height = (int)(width / rawRatio);
      }
      // convert thin image to fat
      else {
        height = Math.min(rawHeight, limitHeight);
        width = (int) (height * rawRatio);
      }

      BufferedImage src = reader.read(0);
      String format = reader.getFormatName();
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics2D = (Graphics2D) bi.getGraphics();
      setGraphics2DHint(graphics2D);
      graphics2D.drawImage(src, 0, 0, width, height, null);
      ImageIO.write(bi, format, new File(toImagePath));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally{
      if (imageInputStream != null) {
        imageInputStream.close();
      }
    }
  }

  /**
   * 重置图形的尺寸, 注意图片将被拉伸或压缩, 以适应新的宽度和高度
   * @param srcImagePath  原图片绝对路径
   * @param toImagePath   目标图片绝对路径
   * @param width         变化后的宽度
   * @param height        变化后的高度
   * @throws IOException
   */
  public static void resizeImage(String srcImagePath,String toImagePath,int width,int height) throws IOException{
    ImageInputStream imageInputStream = null;
    try{
      imageInputStream = ImageIO.createImageInputStream(new File(srcImagePath));
      ImageReader reader = getImageReaderByFile(imageInputStream);
      reader.setInput(imageInputStream, true);
      BufferedImage src = reader.read(0);
      String format = reader.getFormatName();

      // 放大边长
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics2D = (Graphics2D) bi.getGraphics();
      setGraphics2DHint(graphics2D);
      graphics2D.drawImage(src, 0, 0, width, height, null);
      ImageIO.write(bi, format, new File(toImagePath));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally{
      if (imageInputStream != null) {
        imageInputStream.close();
      }
    }
  }
}