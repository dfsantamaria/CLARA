/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.clara.core;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 *
 * @author Daniele Santamaria
 */
public class Configuration 
{
    private Path[] paths;
    public Configuration(int pathSize)
    {
       paths=new Path[pathSize];
    }
    public Path[] getPaths(){return paths;}   
    
}
