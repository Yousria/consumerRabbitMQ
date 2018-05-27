package com.example.dataforlife.consumer.influxdb;


import com.example.dataforlife.consumer.pointservice.InfluxPoint;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.influxdb.InfluxDBConnectionFactory;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by kokoghlanian on 08/05/2018.
 */
@Service
public class InfluxDBServiceImpl implements IInfluxDBService {//, InitializingBean {

    static int cpt = 0;
    @Autowired
    InfluxDBTemplate<Point> influxDBTemplate;

    @Autowired
    InfluxDBConnectionFactory influxDBConnectionFactory;
    private List<InfluxPoint> points = new ArrayList<>();


    public void write(Point point) {
        InfluxDB influxDB= InfluxSingleton.getInstance();
        //influxDBTemplate.write(point);
        influxDB.write(point);
        System.out.println(point.toString());
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void write(List<Point> points) {
        influxDBTemplate.write(points);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void write(BatchPoints bp) {
        InfluxDB influxDB= InfluxSingleton.getInstance();
        influxDB.write(bp);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public Point buildPoint(HashMap<String, Object> fieldMap, String measurement) {
        final Point p = Point.measurement(measurement)
                .time((long) fieldMap.get("time"), TimeUnit.MILLISECONDS)
                .fields(fieldMap)
                .build();

        return p;
    }


    public List<Point> createPoints(String table, ArrayList<HashMap<String, Object>> fieldMap) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < fieldMap.size(); i++) {

        }
        return points;
    }


    public ArrayList<HashMap<String, Object>> createHashMapForPointList(List<InfluxPoint> points, String idUser) {
        ArrayList<HashMap<String, Object>> resultArray = new ArrayList<>();
        for (InfluxPoint pointValue : points) {
            HashMap<String, Object> PointMap = new HashMap<>();
            PointMap.putAll(pointValue.getValue());
            PointMap.put("idUser", idUser);
            PointMap.put("time", pointValue.getTimestamp().toEpochMilli());
            resultArray.add(PointMap);
        }
        return resultArray;
    }

    @Override
    public void createPointInInflux(List<InfluxPoint> pointList, String measurement, String idUser) {

        if (pointList != null) {
            if (!pointList.isEmpty()) {
                points.addAll(pointList);
                if(points.size()>=5) {
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            createPoints(measurement, pointList, idUser);
                        }
                    };
                    t.run();
                    points.clear();
                }
                //if (!points.isEmpty()) {


                  //  write(bp);


                        /* try {
                             System.out.println("in");
                             while(t.isAlive())
                                 TimeUnit.SECONDS.sleep(2);
                             System.out.println("out");
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }*/

                //}


            }
        }
    }

    private void createPoints(String measurement, List<InfluxPoint> pointList, String idUser) {
    InfluxDB influxDB= InfluxSingleton.getInstance();
    BatchPoints batchPoints= BatchPoints.database("dataforlifeDB").build();
        for(InfluxPoint point : pointList){
            Point p = Point.measurement(measurement).fields(point.getValue()).addField("ID",idUser).addField("timestamp",point.getTimestamp().toEpochMilli()).build();
            batchPoints.point(p);
        }
        influxDB.write(batchPoints);
    }
    /*@Override
    public void afterPropertiesSet() throws Exception {
        influxDBTemplate.createDatabase();
    }*/
}
