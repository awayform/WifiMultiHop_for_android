package com.xd.wifimultihop.business.comm;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.socket.UdpSocket;
import com.xd.wifimultihop.business.ui.VideoCallActivity;

public class VideoComm {

	public class MyCamera {
		private Context context;
		private Camera mCamera = null; // Camera对象，相机预览

		public MyCamera(Context context) {
			this.context = context;
		}

		public void closeCamera() {
			if (null != mCamera) {
				mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
				mCamera.stopPreview();
				bIfPreview = false;
				mCamera.release();
				mCamera = null;
			}
		}

		/* 【2】【相机预览】 */
		public void initCamera() {
			// surfaceChanged中调用
			Log.i("TAG", "going into initCamera");
			if (bIfPreview) {
				mCamera.stopPreview();// stopCamera();
			}
			if (null != mCamera) {
				try {
					/* Camera Service settings */
					Camera.Parameters parameters = mCamera.getParameters();
					// parameters.setFlashMode("off"); // 无闪光灯
					// parameters.setPictureFormat(ImageFormat.JPEG);
					// parameters.setPictureFormat(PixelFormat.JPEG); //Sets the
					// image format for picture 设定相片格式为JPEG，默认为NV21
					// parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
					// //Sets the image format for preview picture，默认为NV21
					/*
					 * 【ImageFormat】JPEG/NV16(YCrCb format，used for
					 * Video)/NV21(YCrCb format，used for
					 * Image)/RGB_565/YUY2/YU12
					 */
					// parameters.setPreviewFpsRange(5, 60);
					// 【调试】获取camera支持的PictrueSize，看看能否设置？？
					List<Size> pictureSizes = mCamera.getParameters()
							.getSupportedPictureSizes();
					List<Size> previewSizes = mCamera.getParameters()
							.getSupportedPreviewSizes();
					List<Integer> previewFormats = mCamera.getParameters()
							.getSupportedPreviewFormats();
					List<int[]> previewFpsRange = mCamera.getParameters()
							.getSupportedPreviewFpsRange();
					Log.i("TAG" + "initCamera", "cyy support parameters is ");
					Size psize = null;
					for (int i = 0; i < pictureSizes.size(); i++) {
						psize = pictureSizes.get(i);
						Log.i("TAG" + "initCamera", "PictrueSize,width: "
								+ psize.width + " height" + psize.height);
					}
					for (int i = 0; i < previewSizes.size(); i++) {
						psize = previewSizes.get(i);
						Log.i("TAG" + "initCamera", "PreviewSize,width: "
								+ psize.width + " height" + psize.height);
					}
					Integer pf = null;
					for (int i = 0; i < previewFormats.size(); i++) {
						pf = previewFormats.get(i);
						Log.i("TAG" + "initCamera", "previewformates:" + pf);
					}
					int[] fps = new int[2];
					for (int i = 0; i < previewFpsRange.size(); i++) {
						fps = previewFpsRange.get(i);
						Log.i("TAG" + "initCamera", "previewFpsRange:" + fps[0]
								+ "," + fps[1]);
					}
					// 设置拍照和预览图片大小
					// parameters.setPictureSize(640, 480); //指定拍照图片的大小
					parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); // 指定preview的大小
					// 这两个属性 如果这两个属性设置的和真实手机的不一样时，就会报错

					// 横竖屏镜头自动调整
					if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
						parameters.set("orientation", "portrait"); //
						parameters.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍）
						mCamera.setDisplayOrientation(90); // 在2.2以上可以使用
					} else// 如果是横屏
					{
						parameters.set("orientation", "landscape"); //
						mCamera.setDisplayOrientation(0); // 在2.2以上可以使用
					}

					/* 视频流编码处理 */
					// 添加对视频流处理函数
					// 【获取视频预览帧的接口】
					PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
						@Override
						public void onPreviewFrame(byte[] data, Camera camera) {
							int width = camera.getParameters().getPreviewSize().width;
							int height = camera.getParameters()
									.getPreviewSize().height;
							Log.i("TAG", "nv21--->" + data.length);
							new SendThread(data, width, height).start();
						}// endonPriview
					};
					mCamera.setPreviewCallback(mJpegPreviewCallback);

					// 设定配置参数并开启预览
					mCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera
					mCamera.startPreview(); // 打开预览画面
					bIfPreview = true;

					// 【调试】设置后的图片大小和预览大小以及帧率
					Camera.Size csize = mCamera.getParameters()
							.getPreviewSize();
					mPreviewHeight = csize.height; //
					mPreviewWidth = csize.width;
					Log.i("TAG" + "initCamera",
							"after setting, previewSize:width: " + csize.width
									+ " height: " + csize.height);
					csize = mCamera.getParameters().getPictureSize();
					Log.i("TAG" + "initCamera",
							"after setting, pictruesize:width: " + csize.width
									+ " height: " + csize.height);
					Log.i("TAG" + "initCamera",
							"after setting, previewformate is "
									+ mCamera.getParameters()
											.getPreviewFormat());
					int[] fpsRange = new int[2];
					mCamera.getParameters().getPreviewFpsRange(fpsRange);
					Log.i("TAG" + "initCamera",
							"after setting, previewframetate is " + fpsRange[0]
									+ fpsRange[1]);
					// Log.i("TAG",
					// mCamera.getParameters().getSupportedPreviewFormats().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void openCamera(int frontOrBack, SurfaceHolder holder) {
			mCamera = Camera.open(1);// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
			try {
				Log.i("TAG", "SurfaceHolder.Callback：surface Created");
				mCamera.setPreviewDisplay(holder);// set the surface to be used
													// for live preview
			} catch (Exception ex) {
				if (null != mCamera) {
					mCamera.release();
					mCamera = null;
				}
				Log.i("TAG" + "initCamera", ex.getMessage());
			}
		}
	}

	private class RecvThread extends Thread {

		boolean runFlag = true;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (runFlag) {
				byte[] data = mUdpSocket.udpRecv();
				if (data != null) {
					bmpDecode = BitmapFactory.decodeByteArray(data, 0,
							data.length);
				}
				// Log.i("TAG", "decode:"+data.length);
				// mImageView.setImageBitmap(bmpdecode);
				Message msg = new Message();
				msg.obj = bmpDecode;
				if (VideoCallActivity.mHandler != null) {
					VideoCallActivity.mHandler.sendMessage(msg);
				}
			}
		}

	}

	private class SendThread extends Thread {

		byte[] data;
		int width;
		int height;

		public SendThread(byte[] data, int width, int height) {
			super();
			this.data = data;
			this.width = width;
			this.height = height;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			int[] rgb = new int[width * height];
			Yuv420spToRgb(rgb, data, width, height);
			// Log.i("TAG", "rgb--->"+rgb.length);
			Bitmap bmp = Bitmap.createBitmap(rgb, 0, width, width, height,
					Bitmap.Config.ARGB_4444);
			mMatrix.reset();
			mMatrix.postRotate(270.0f);
			Bitmap nbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), mMatrix, true);
			// mImageView.setImageBitmap(nbmp);
			// 压缩图片

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			nbmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
			byte[] buffer = baos.toByteArray();
			Log.i("TAG", "jpeg--->" + buffer.length);
			mUdpSocket.udpSend(buffer);
		}

	}

	private static VideoComm mVideoComm = null;

	public static VideoComm getInstance(Context context, String dstAddr) {
		if (mVideoComm == null) {
			mVideoComm = new VideoComm(context, dstAddr);
		}
		return mVideoComm;
	}

	static public void Yuv420spToRgb(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	private Matrix mMatrix;
	private UdpSocket mUdpSocket;
	private RecvThread mRecvThread;

	public MyCamera myCamera;

	private int mPreviewWidth = 320;

	private int mPreviewHeight = 240;

	private boolean bIfPreview;

	private Bitmap bmpDecode;

	public VideoComm(Context context, String dstAddr) {
		// TODO Auto-generated constructor stub
		myCamera = new MyCamera(context);
		mMatrix = new Matrix();
		mUdpSocket = new UdpSocket(Constants.UDP_VIDEO_PORT,
				Constants.UDP_VIDEO_PACKET);
		if (dstAddr == null) {
			Log.e("TAG", "address is null !!!");
		}
		mUdpSocket.udpSendSetting(dstAddr);
		mRecvThread = new RecvThread();
		mRecvThread.start();
	}

	public void videoEnd() {
		mRecvThread.runFlag = false;
		mUdpSocket.udpEnd();
		mVideoComm = null;
	}

}
