package cn.appsdream.nestrefresh2;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class Transform {
    public boolean transformNestScroll(int viewType, int[] nestscroll, int[] transformed) {
        if (viewType == 1 && nestscroll[1] < 0) {
//            transformed[1] = nestscroll[1] / 2;
            transformed[1] = getInterpolation(nestscroll[1]);
            return true;
        }
        if (viewType == 2 && nestscroll[1] > 0) {
//            transformed[1] = nestscroll[1] / 2;
            transformed[1] = getInterpolation(nestscroll[1]);
            return true;
        }
        if (viewType == 3 && nestscroll[0] < 0) {
//            transformed[0] = nestscroll[0] / 2;
            transformed[0] = getInterpolation(nestscroll[0]);
            return true;
        }
        if (viewType == 4 && nestscroll[0] > 0) {
//            transformed[0] = nestscroll[0] / 2;
            transformed[0] = getInterpolation(nestscroll[0]);
            return true;
        }
        return false;
    }

    public int getInterpolation(int input) {
        int result;
        int tmp =Math.abs(input);
        if (tmp<300)
            result= tmp/2;
        else if (tmp<500)
            result= 150 + (tmp-300)/3;
       else result=  216 + (tmp-500)/4;
        result = input<0?-result:result;
        return result;
    }
}
