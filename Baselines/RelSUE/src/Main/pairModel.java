package Main;

public class pairModel {

    public Integer valX = 0;
    public Integer valY = 0;

    public pairModel(Integer x, Integer y)
    {
        valX = x;
        valY = y;
    }

    public Integer cmp(pairModel ot)
    {
        if( this.valX.equals(ot.valX) && this.valY.equals(ot.valY) ) return 0;
        if( (this.valX.compareTo(ot.valX) < 0) || ( this.valX.equals(ot.valX) && (this.valY.compareTo(ot.valY) < 0) ) ) return -1;
        else return 1;
    }

    public Boolean equals(pairModel ot)
    {
        return ( this.valX.equals(ot.valX) && this.valY.equals(ot.valY) ) || ( this.valX.equals(ot.valY) && this.valY.equals(ot.valX) );
    }

    public int hashCode()
    {
        return valX * 10 + valY;
    }

}
