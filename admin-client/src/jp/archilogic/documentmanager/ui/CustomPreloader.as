package jp.archilogic.documentmanager.ui {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Sprite;
    import flash.events.Event;
    import flash.events.ProgressEvent;
    import flash.events.TimerEvent;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.utils.Timer;
    import flash.utils.getTimer;
    import mx.core.BitmapAsset;
    import mx.events.FlexEvent;
    import mx.preloaders.IPreloaderDisplay;
    import jp.archilogic.documentmanager.resource.ResourceCommon;

    public class CustomPreloader extends Sprite implements IPreloaderDisplay {
        private static const FADE_DURATION : Number = 500;

        private static const PROGRESS_WIDTH : Number = 280;

        public function CustomPreloader() {
            super();
        }

        private var canvasBitmap : Bitmap;

        private var logo : BitmapAsset;

        private var byteLoaded : Number;

        private var byteTotal : Number;

        private var timer : Timer;

        private var timerStart : int;

        public function get backgroundAlpha() : Number {
            return 0;
        }

        public function set backgroundAlpha( value : Number ) : void {
        }

        public function get backgroundColor() : uint {
            return 0;
        }

        public function set backgroundColor( value : uint ) : void {
        }

        public function get backgroundImage() : Object {
            return null;
        }

        public function set backgroundImage( value : Object ) : void {
        }

        public function get backgroundSize() : String {
            return null;
        }

        public function set backgroundSize( value : String ) : void {
        }

        public function initialize() : void {
            logo = new ResourceCommon.LOGO_APP();

            canvasBitmap = new Bitmap( new BitmapData( 350 , 100 ) );

            addChild( canvasBitmap );

            centering();

            init();
        }

        public function set preloader( obj : Sprite ) : void {
            obj.addEventListener( ProgressEvent.PROGRESS , progressHandler );
            obj.addEventListener( FlexEvent.INIT_COMPLETE , initCompleteHandler );
        }

        public function get progress() : Number {
            if ( isNaN( byteTotal ) || byteTotal == 0 ) {
                return 0;
            }

            return byteLoaded / byteTotal;
        }

        public function get stageHeight() : Number {
            return 0;
        }

        public function set stageHeight( value : Number ) : void {
        }

        public function get stageWidth() : Number {
            return 0;
        }

        public function set stageWidth( value : Number ) : void {
        }

        private function centering() : void {
            x = Math.max( ( root.width - width ) / 2 , 0 );
            y = Math.max( ( root.height - height ) / 2 , 0 );
        }

        private function drawProgress() : void {

            var w : Number = Math.round( PROGRESS_WIDTH * progress );

            canvasBitmap.bitmapData.fillRect( new Rectangle( ( width - PROGRESS_WIDTH ) / 2 , 70 , w , 10 ) ,
                                                             0xff92a3bf );

            canvasBitmap.bitmapData.fillRect( new Rectangle( w + ( width - PROGRESS_WIDTH ) / 2 , 70 ,
                                                             PROGRESS_WIDTH - w , 10 ) , 0xffffffff );
        }

        private function init() : void {
            canvasBitmap.bitmapData.fillRect( new Rectangle( 0 , 0 , width , height ) , 0xff526d9a );

            var rect : Rectangle = new Rectangle( 0 , 0 , logo.width , logo.height );
            var point : Point = new Point( Math.max( ( width - logo.width ) / 2 , 0 ) , 20 );

            canvasBitmap.bitmapData.copyPixels( logo.bitmapData , rect , point , null , null , true );

            canvasBitmap.bitmapData.fillRect( new Rectangle( ( width - PROGRESS_WIDTH ) / 2 , 70 , PROGRESS_WIDTH , 10 ) ,
                                                             0xffffffff );
        }

        private function initCompleteHandler( event : FlexEvent ) : void {
            timer = new Timer( 30 );
            timer.addEventListener( TimerEvent.TIMER , timerHandler );
            timerStart = getTimer();
            timer.start();
        }

        private function progressHandler( event : ProgressEvent ) : void {
            byteLoaded = event.bytesLoaded;
            byteTotal = event.bytesTotal;

            drawProgress();
        }

        private function timerHandler( event : TimerEvent ) : void {
            var alpha : Number = Math.max( 1 - ( getTimer() - timerStart ) / FADE_DURATION , 0 );

            this.alpha = alpha;

            if ( alpha == 0 ) {
                timer.stop();
                dispatchEvent( new Event( Event.COMPLETE ) );
            }
        }
    }
}