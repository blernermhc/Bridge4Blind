// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;


/***********************************************************************
 * TextField with prompt
 * https://stackoverflow.com/questions/1738966/java-jtextfield-with-input-hint
 ***********************************************************************/
public class HintTextFieldUI extends BasicTextFieldUI implements FocusListener
{

    private String m_hint;
    private boolean m_hideOnFocus;
    private Color m_color;

    public Color getColor() {
        return m_color;
    }

    public void setColor(Color color) {
        m_color = color;
        repaint();
    }

    private void repaint() {
        if(getComponent() != null) {
            getComponent().repaint();           
        }
    }

    public boolean isHideOnFocus() {
        return m_hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus) {
        m_hideOnFocus = hideOnFocus;
        repaint();
    }

    public String getHint() {
        return m_hint;
    }

    public void setHint(String hint) {
        m_hint = hint;
        repaint();
    }
    public HintTextFieldUI(String hint) {
        this(hint,false);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus) {
        this(hint,hideOnFocus, null);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus, Color color) {
        m_hint = hint;
        m_hideOnFocus = hideOnFocus;
        m_color = color;
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(g);
        JTextComponent comp = getComponent();
        if(m_hint!=null && comp.getText().length() == 0 && (!(m_hideOnFocus && comp.hasFocus()))){
            if(m_color != null) {
                g.setColor(m_color);
            } else {
                g.setColor(comp.getForeground().brighter().brighter().brighter());              
            }
            int padding = (comp.getHeight() - comp.getFont().getSize())/2;
            g.drawString(m_hint, 7, comp.getHeight()-padding-1);          
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(m_hideOnFocus) repaint();

    }

    @Override
    public void focusLost(FocusEvent e) {
        if(m_hideOnFocus) repaint();
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        getComponent().addFocusListener(this);
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        getComponent().removeFocusListener(this);
    }
}
